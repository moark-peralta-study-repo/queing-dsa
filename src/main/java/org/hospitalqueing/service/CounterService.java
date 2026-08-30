package org.hospitalqueing.service;

import java.util.List;

import org.hospitalqueing.dao.CounterDAO;
import org.hospitalqueing.model.Counter;

public class CounterService {
  private final CounterDAO counterDAO;

  public CounterService(CounterDAO counterDAO) {
    this.counterDAO = counterDAO;
  }

  public void createCounter(Counter counter) {
    counterDAO.save(counter);
  }

  public Counter getCounterById(int counterId) {
    return counterDAO.findById(counterId);
  }

  public List<Counter> getAllCounters() {
    return counterDAO.findAll();
  }

  public List<Counter> getCountersByDepartment(int departmentId) {
    return counterDAO.findByDepartment(departmentId);
  }

  public void updateCounter(Counter counter) {
    counterDAO.update(counter);
  }

  public void deleteCounter(int counterId) {
    counterDAO.delete(counterId);
  }
}
