package asynchJavaParser.common.utils;

import asynchJavaParser.common.reports.interfaces.*;
import asynchJavaParser.eventDrivenJavaParser.client.projectAnalysis.ClassElem;
import asynchJavaParser.eventDrivenJavaParser.client.projectAnalysis.InterfaceElem;
import asynchJavaParser.eventDrivenJavaParser.client.projectAnalysis.PackageElem;
import asynchJavaParser.eventDrivenJavaParser.client.projectAnalysis.ProjectStructure;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to create and modify a JTree component
 * according to the structure of the class/interface/package/project provided
 * */
public class TreePanelController extends JPanel {

    private JTree tree;

    private DefaultMutableTreeNode root;

    public TreePanelController() {
        createTree();
    }

    public JTree getTree() {
        return this.tree;
    }

    public DefaultMutableTreeNode getRoot() {
        return this.root;
    }

    public void resetTree() {
        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) model.getRoot();
        treeRoot.removeAllChildren();
        model.reload();
    }

    // tree update for getInterfaceReport if selected a class
    public void updateClassReport(final IClassReport report, final DefaultMutableTreeNode addNode) {
        List<IMethodInfo> methodInfo = new ArrayList<>();
        List<IFieldInfo> fieldInfo = new ArrayList<>();
        methodInfo.addAll(report.getMethodsInfo());
        fieldInfo.addAll(report.getFieldsInfo());

        DefaultMutableTreeNode className;
        List<DefaultMutableTreeNode> fieldName = new ArrayList<>();
        List<DefaultMutableTreeNode> methodName = new ArrayList<>();
        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode("FIELDS");
        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode("METHODS");

        className = new DefaultMutableTreeNode(report.getFullClassName() + " - Class");
        className.add(new DefaultMutableTreeNode(report.getSrcFullFileName() + " - SrcFullFileName"));
        addNode.add(className);

        className.add(fieldNode);
        className.add(methodNode);

        fieldInfo.forEach(f -> {
            DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(f.getName());
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(f.getFieldTypeFullName() + " - Type");
            fieldName.add(nameNode);
            nameNode.add(typeNode);
        });
        fieldName.forEach(fn -> fieldNode.add(fn));

        methodInfo.forEach(m -> {
            DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(m.getName());
            DefaultMutableTreeNode retTypeNode = new DefaultMutableTreeNode(m.getReturnType() + " - ReturnType");
            methodName.add(nameNode);
            nameNode.add(retTypeNode);
            m.getParameters().forEach(p -> {
                DefaultMutableTreeNode parameter = new DefaultMutableTreeNode(p.getName() + " - Parameter");
                nameNode.add(parameter);
                parameter.add(new DefaultMutableTreeNode(p.getType() + " - ParamType"));
            });
        });
        methodName.forEach(m -> methodNode.add(m));
    }

    // tree update for getClassReport if selected an interface
    public void updateInterfaceReport(final IInterfaceReport report, final DefaultMutableTreeNode addNode) {
        List<IMethodInfo> methodInfo = new ArrayList<>(report.getMethodsInfo());

        DefaultMutableTreeNode interfaceName;
        List<DefaultMutableTreeNode> methodName = new ArrayList<>();
        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode("METHODS");

        interfaceName = new DefaultMutableTreeNode(report.getFullInterfaceName() + " - Interface");
        interfaceName.add(new DefaultMutableTreeNode(report.getSrcFullFileName() + " - SrcFullFileName"));
        addNode.add(interfaceName);

        interfaceName.add(methodNode);

        methodInfo.forEach(m -> {
            DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(m.getName());
            DefaultMutableTreeNode retTypeNode = new DefaultMutableTreeNode(m.getReturnType() + " - ReturnType");
            methodName.add(nameNode);
            nameNode.add(retTypeNode);
            m.getParameters().forEach(p -> {
                DefaultMutableTreeNode parameter = new DefaultMutableTreeNode(p.getName() + " - Parameter");
                nameNode.add(parameter);
                parameter.add(new DefaultMutableTreeNode(p.getType() + " - ParamType"));
            });
        });
        methodName.forEach(m -> methodNode.add(m));
    }

    // tree update for getPackageReport
    public void updatePackageReport(final IPackageReport report, final DefaultMutableTreeNode addNode) {
        Map<String, List<IReport>> files;

        files = Stream.concat(report.getInterfaceReports().stream(), report.getClassReports().stream())
                .collect(Collectors.groupingBy(IReport::getSrcFullFileName));

        files.forEach((k, v) -> {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode("CLASSES");
            DefaultMutableTreeNode interfaceNode = new DefaultMutableTreeNode("INTERFACES");
            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(k + " - package");

            packageNode.add(classNode);
            packageNode.add(interfaceNode);
            addNode.add(packageNode);
            v.forEach(x -> {
                if (String.valueOf(x.getClass()).contains("InterfaceReport")) {
                    updateInterfaceReport((IInterfaceReport) x, interfaceNode);
                }
                if (String.valueOf(x.getClass()).contains("ClassReport")) {
                    updateClassReport((IClassReport) x, classNode);
                }
            });
        });
    }

    // tree update for getProjectReport
    public void updateProjectReport(final IProjectReport report, final DefaultMutableTreeNode addNode) {
        Map<String, List<IPackageReport>> packageMap;
        List<IPackageReport> packageInfo = new ArrayList<>(report.getPackageReports());

        DefaultMutableTreeNode projectName;
        DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode("PACKAGES");


        projectName = new DefaultMutableTreeNode(report.getMainClass() + " - MainClass");
        addNode.add(projectName);

        projectName.add(packageNode);

        packageMap = packageInfo.stream().collect(Collectors.groupingBy(x -> x.getSrcFullFileName()));

        packageMap.forEach((k, v) -> {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode("CLASSES");
            DefaultMutableTreeNode interfaceNode = new DefaultMutableTreeNode("INTERFACES");
            DefaultMutableTreeNode pName = new DefaultMutableTreeNode(k);
            packageNode.add(pName);

            pName.add(classNode);
            pName.add(interfaceNode);
            v.forEach(x -> {
                if (!x.getClassReports().isEmpty()){
                    x.getClassReports().forEach(c -> updateClassReport(c,classNode));
                }
                if (!x.getInterfaceReports().isEmpty()) {
                    x.getInterfaceReports().forEach(i -> updateInterfaceReport(i,interfaceNode));
                }
            });
        });
    }

    // tree dynamic update for analyzeProject in eventDrivenJavaParser
    public void dynamicUpdate(final ProjectStructure ps) {
        PackageElem psRoot = ps.getRoot();
        resetTree();

        DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode("PACKAGES");
        Map<String, PackageElem> innerPackages;

        if(!psRoot.getInnerPackages().isEmpty()){
            this.root.add(packageNode);
            innerPackages = psRoot.getInnerPackages();
            innerPackages.forEach((k, v) -> {
                DefaultMutableTreeNode packageName = new DefaultMutableTreeNode(k+ " - Package");
                packageNode.add(packageName);
                packageUpdate(v, packageName);
            });
        }

        classUpdate(psRoot, this.root);
        interfaceUpdate(psRoot, this.root);
    }

    private void createTree() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.root = new DefaultMutableTreeNode("* Tree *");
        DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
        render.setLeafIcon(null);
        render.setOpenIcon(null);
        this.tree = new JTree(this.root);
        this.tree.setCellRenderer(render);
    }

    private void classUpdate(final PackageElem packageElem, final DefaultMutableTreeNode addNode){
        if (!packageElem.getInnerClasses().isEmpty()) {
            Map<String, ClassElem> innerClasses = packageElem.getInnerClasses();
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode("CLASSES");

            addNode.add(classNode);

            innerClasses.forEach((k, v) -> {
                if(Optional.of(v.getElem()).get().isPresent()){
                    DefaultMutableTreeNode name = new DefaultMutableTreeNode(k+ " - Class");

                    classNode.add(name);

                    ClassOrInterfaceDeclaration c = Optional.of(v.getElem()).get().get();

                    name.add(new DefaultMutableTreeNode(c.getFullyQualifiedName().get()  + " - SrcFullFileName"));

                    if(!Optional.of(v).get().getFields().isEmpty()){
                        List<DefaultMutableTreeNode> fieldsName = new ArrayList<>();
                        DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode("FIELDS");

                        name.add(fieldNode);

                        v.getFields().forEach(f -> {
                            f.forEach(x -> {
                                DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(x.getNameAsString());
                                DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(x.getTypeAsString() + " - Type");
                                fieldsName.add(nameNode);
                                nameNode.add(typeNode);
                            });
                        });
                        fieldsName.forEach(fn -> fieldNode.add(fn));
                    }

                    if (!v.getMethods().isEmpty()){
                        List<DefaultMutableTreeNode> methodsName = new ArrayList<>();
                        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode("METHODS");

                        name.add(methodNode);

                        v.getMethods().forEach((x, y) -> {
                            DefaultMutableTreeNode mName = new DefaultMutableTreeNode(x);
                            DefaultMutableTreeNode retTypeNode = new DefaultMutableTreeNode(y.getType() + " - ReturnType");
                            methodsName.add(mName);
                            mName.add(retTypeNode);

                            y.getParameters().forEach(p -> {
                                DefaultMutableTreeNode parameter = new DefaultMutableTreeNode(p.getNameAsString() + " - Parameter");
                                mName.add(parameter);
                                parameter.add(new DefaultMutableTreeNode(p.getType() + " - ParamType"));
                            });
                        });
                        methodsName.forEach(m -> methodNode.add(m));
                    }
                }
            });
        }
    }

    private void interfaceUpdate(final PackageElem packageElem, final DefaultMutableTreeNode addNode){
        if (!packageElem.getInnerInterfaces().isEmpty()) {
            Map<String, InterfaceElem> innerInterfaces = packageElem.getInnerInterfaces();
            DefaultMutableTreeNode interfaceNode = new DefaultMutableTreeNode("INTERFACES");

            addNode.add(interfaceNode);

            innerInterfaces.forEach((k, v) -> {
                if(Optional.of(v.getElem()).get().isPresent()){
                    DefaultMutableTreeNode interfaceName = new DefaultMutableTreeNode(k+ " - Interface");

                    interfaceNode.add(interfaceName);

                    if (!v.getMethods().isEmpty()){
                        List<DefaultMutableTreeNode> methodsName = new ArrayList<>();
                        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode("METHODS");

                        interfaceName.add(methodNode);
                        v.getMethods().forEach((x, y) -> {
                            DefaultMutableTreeNode mName = new DefaultMutableTreeNode(y.getNameAsString());
                            DefaultMutableTreeNode retTypeNode = new DefaultMutableTreeNode(y.getType() + " - ReturnType");
                            methodsName.add(mName);
                            mName.add(retTypeNode);

                            y.getParameters().forEach(p -> {
                                DefaultMutableTreeNode parameter = new DefaultMutableTreeNode(p.getNameAsString() + " - Parameter");
                                mName.add(parameter);
                                parameter.add(new DefaultMutableTreeNode(p.getType() + " - ParamType"));
                            });
                        });
                        methodsName.forEach(m -> methodNode.add(m));
                    }
                }
            });
        }
    }

    private void packageUpdate(final PackageElem packageElem, final DefaultMutableTreeNode addNode){
        if (!packageElem.getInnerPackages().isEmpty()) {
            Map<String, PackageElem> packages = packageElem.getInnerPackages();
            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode("PACKAGES");

            addNode.add(packageNode);

            packages.forEach((k, v) -> {
                DefaultMutableTreeNode packName = new DefaultMutableTreeNode(k+ " - Package");
                packageNode.add(packName);
                packageUpdate(v, packName);
            });
        }

        classUpdate(packageElem, addNode);
        interfaceUpdate(packageElem, addNode);
    }
}
