package org.example.lab7_tarea_opcional.respository;


import org.example.lab7_tarea_opcional.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    boolean existsByDocument(String document);
    boolean existsByDocumentAndIdNot(String document, Integer id);
}
