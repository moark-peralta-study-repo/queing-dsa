package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.ServiceDAO;
import org.hospitalqueing.model.Service;

public class ServiceCatalogService {
  private final ServiceDAO serviceDAO;

  public ServiceCatalogService(ServiceDAO serviceDAO) {
    this.serviceDAO = serviceDAO;
  }

  public void createService(Service service) {
    serviceDAO.save(service);
  }

  public Service getServiceById(int serviceId) {
    return serviceDAO.findById(serviceId);
  }

  public List<Service> getAllServices() {
    return serviceDAO.findAll();
  }

  public void updateService(Service service) {
    serviceDAO.update(service);
  }

  public void deleteService(int serviceId) {
    serviceDAO.delete(serviceId);
  }
}
