package asynchJavaParser.eventDrivenJavaParser.client;

import asynchJavaParser.common.reports.interfaces.IProjectReport;
import io.vertx.core.Future;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class to handle the action on getProjectReport button
 * calling the getProjectReport library function
 * */
public class GetProjectReport implements ActionListener {

    private final VisualizerFrame view;

    public GetProjectReport(final VisualizerFrame frame) {
        this.view = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.view.resetTree();
        this.view.getStopButton().setEnabled(false);
        this.view.getMethodButtons().get("getProjectReport").setEnabled(false);
        Future<IProjectReport> future = this.view.getLib().getProjectReport(this.view.getNameDirectory().getText());
        future.onSuccess(res -> {
            this.view.getTreePanel().updateProjectReport(res, this.view.getTreePanel().getRoot());
        });
        future.onFailure(res -> {
            this.view.errorMessage("Error!!! Select a project");
        });
    }
}
