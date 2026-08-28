package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Service;
import org.hospitalqueing.service.ServiceCatalogService;

public class ServiceController {
  private final ServiceCatalogService serviceCatalogService;

  public ServiceController(ServiceCatalogService serviceCatalogService) {
    this.serviceCatalogService = serviceCatalogService;
  }

  public void createService(Service service) {
    serviceCatalogService.createService(service);
  }

  public Service getService(int serviceId) {
    return serviceCatalogService.getServiceById(serviceId);
  }

  public List<Service> getAllServices() {
    return serviceCatalogService.getAllServices();
  }

  public void updateService(Service service) {
    serviceCatalogService.updateService(service);
  }

  public void deleteService(int serviceId) {
    serviceCatalogService.deleteService(serviceId);
  }
}
