package org.hospitalqueing.web;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import io.javalin.Javalin;
import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.dao.DepartmentDAO;
import org.hospitalqueing.dao.QueueEntryDAO;
import org.hospitalqueing.dao.QueueEventDAO;
import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Department;
import org.hospitalqueing.model.QueueEntry;
import org.hospitalqueing.model.TicketStatus;
import org.hospitalqueing.service.QueueManagementService;

public class WebServer {

  private final Gson gson = new Gson();

  public WebServer(int port) {
    QueueEntryDAO queueEntryDAO = new QueueEntryDAO();
    QueueEventDAO queueEventDAO = new QueueEventDAO();
    ServiceDAO serviceDAO = new ServiceDAO();
    DepartmentDAO departmentDAO = new DepartmentDAO();
    CounterDAO counterDAO = new CounterDAO();
    QueueManagementService queueManagementService =
        new QueueManagementService(queueEntryDAO, queueEventDAO, serviceDAO, departmentDAO, counterDAO);

    Javalin app = Javalin.create();

    app.before(ctx -> {
      ctx.header("Access-Control-Allow-Origin", "*");
      ctx.header("Access-Control-Allow-Methods", "GET, OPTIONS");
      ctx.header("Access-Control-Allow-Headers", "Content-Type");
    });

    app.get(
        "/api/health",
        ctx -> {
          ctx.contentType("application/json");
          ctx.result(gson.toJson(Map.of("status", "ok")));
        });

    app.get(
        "/api/ticket/{token}",
        ctx -> {
          TicketStatus ticket = queueManagementService.getTicketStatus(ctx.pathParam("token"));
          ctx.contentType("application/json");
          if (ticket == null) {
            ctx.status(404);
            ctx.result(gson.toJson(Map.of("error", "ticket not found")));
            return;
          }
          ctx.result(gson.toJson(ticket));
        });

    app.get(
        "/api/queue/{departmentId}",
        ctx -> {
          int departmentId = Integer.parseInt(ctx.pathParam("departmentId"));
          List<QueueEntry> active = queueManagementService.getActiveQueue(departmentId);
          ctx.contentType("application/json");
          ctx.result(gson.toJson(active));
        });

    app.get(
        "/api/departments",
        ctx -> {
          List<Department> departments = departmentDAO.findAll();
          ctx.contentType("application/json");
          ctx.result(gson.toJson(departments));
        });

    app.start(port);
  }
}
