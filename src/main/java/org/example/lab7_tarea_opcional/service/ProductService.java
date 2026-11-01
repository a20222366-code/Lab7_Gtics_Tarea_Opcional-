package org.example.lab7_tarea_opcional.service;


import org.example.lab7_tarea_opcional.entity.Product;
import org.example.lab7_tarea_opcional.respository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean nombreDuplicado(String name, Integer id) {
        if (id == null) {
            return productRepository.existsByName(name);
        }
        return productRepository.existsByNameAndIdNot(name, id);
    }

    public Product guardar(Product product) {
        return productRepository.save(product);
    }

}
