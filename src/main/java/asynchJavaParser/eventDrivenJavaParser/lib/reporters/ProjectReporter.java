package asynchJavaParser.eventDrivenJavaParser.lib.reporters;

import asynchJavaParser.common.reports.ProjectReport;
import asynchJavaParser.common.reports.interfaces.IProjectReport;
import asynchJavaParser.common.utils.FileExplorer;
import asynchJavaParser.common.visitors.ProjectVisitor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ProjectReporter extends AbstractVerticle {
    private final Promise<IProjectReport> res;
    private final String path;
    private final FileExplorer fileExplorer;

    public ProjectReporter(Promise<IProjectReport> res, String path){
        this.res = res;
        this.path = path;
        this.fileExplorer = new FileExplorer(this.path);
    }

    @Override
    public void start() {
        CompilationUnit cu;
        if (this.fileExplorer.getPath().contains(".java")) {
            res.fail("invalid path for Project Report");
            return;
        }
        List<String> packages = this.fileExplorer.getAllSubpackageFiles();
        ProjectReport projectReport = new ProjectReport();
        // System.out.println("ALL PACKAGES: " + packages); // for debug purposes
        for (String p : packages) {
            // System.out.println("package and its content: " + p); // for debug purposes
            try {
                // log("Project reporter started...");
                cu = StaticJavaParser.parse(new File(p));
                ProjectVisitor visitor = new ProjectVisitor();
                visitor.visit(cu, projectReport);
            } catch (FileNotFoundException e) {
                log("Project reporter failed...");
                res.fail("invalid path");
            }
        }
        res.complete(projectReport);
    }
    private static void log(String msg) {
        System.out.println("" + Thread.currentThread() + " " + msg);
    }

}
