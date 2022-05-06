package asynchJavaParser.eventDrivenJavaParser.reports;

import asynchJavaParser.eventDrivenJavaParser.reports.interfaces.IClassReport;
import asynchJavaParser.eventDrivenJavaParser.reports.interfaces.IProjectReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectReport implements IProjectReport {
  private IClassReport mainClass;
  private Map<String, IClassReport> allClasses;

  public ProjectReport(){}

  public ProjectReport(IClassReport mainClass, Map<String, IClassReport> allClasses) {
    this.mainClass = mainClass;
    this.allClasses = allClasses;
  }

  @Override
  public IClassReport getMainClass() {
    return this.mainClass;
  }

  @Override
  public List<IClassReport> getAllClasses() {
    return new ArrayList<>(this.allClasses.values());
  }

  @Override
  public IClassReport getClassReport(String fullClassName) {
    return this.allClasses.get(fullClassName);
  }
}
