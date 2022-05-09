package asynchJavaParser.eventDrivenJavaParser.lib.reporters;

import asynchJavaParser.eventDrivenJavaParser.lib.reports.ClassReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.InterfaceReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.PackageReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.interfaces.IClassReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.interfaces.IInterfaceReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.interfaces.IPackageReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.interfaces.IProjectReport;
import asynchJavaParser.eventDrivenJavaParser.lib.reports.ProjectReport;
import asynchJavaParser.eventDrivenJavaParser.lib.visitors.ProjectVisitor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.io.File;
import java.io.FileNotFoundException;

public class ProjectReporter extends AbstractVerticle {
    private final Promise<IProjectReport> res;
    private final String path;

    public ProjectReporter(Promise<IProjectReport> res, String path){
        this.res = res;
        this.path = path;
    }

    @Override
    public void start() throws Exception {
        CompilationUnit cu = null;
        try {
            log("task started...");
            cu = StaticJavaParser.parse(new File(this.path));
            IProjectReport projectReport = new ProjectReport();
            IPackageReport packageReport = new PackageReport();
            IClassReport classReport = new ClassReport();
            IInterfaceReport interfaceReport = new InterfaceReport();
            ProjectVisitor visitor = new ProjectVisitor(projectReport, packageReport, classReport, interfaceReport);
            visitor.visit(cu, null);
            res.complete(projectReport);
        } catch (FileNotFoundException e) {
            log("task failed...");
            res.fail("invalid path");
        }
    }
    private static void log(String msg) {
        System.out.println("" + Thread.currentThread() + " " + msg);
    }
}