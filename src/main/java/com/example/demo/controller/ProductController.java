package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.IProductService;
import com.example.demo.service.ProductService;
import com.example.demo.util.SearchOperation;
import com.google.common.base.Joiner;
import dao.ProductSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@CrossOrigin("*")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRepository repository;

    @GetMapping("")
    public ResponseEntity<Iterable<Product>> getAllProduct(){
        return new ResponseEntity<>(productService.getAll(), HttpStatus.OK);
    }
    @PostMapping("")
    public ResponseEntity<Product> saveProduct(@RequestBody Product product){
        return new ResponseEntity<>(productService.save(product), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id){
        Optional<Product> productOptional = productService.findByID(id);
        return productOptional.map(product -> new ResponseEntity<>(product,HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Product> editProduct(@PathVariable("id") Long id, @RequestBody Product product){
        Optional<Product> productOptional = productService.findByID(id);
        return productOptional.map(product1 -> {
            product.setId(product1.getId());
            if (product.getName().equalsIgnoreCase("")){
            product.setName(product1.getName());}
            return new ResponseEntity<>(productService.save(product),HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id){
        Optional<Product> productOptional = productService.findByID(id);
        return productOptional.map(product -> {
            productService.remove(id);
            return new ResponseEntity<Product>(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @GetMapping("/spec")
    @ResponseBody
    public ResponseEntity<List<Product>> findAllBySpecification(@RequestParam(value = "search") String search){
        ProductSpecificationBuilder builder = new ProductSpecificationBuilder();
        String operationSetExper = Joiner.on("|").join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile("(\\w+?)(" + operationSetExper + ")(\\p{Punct}?)(\\w+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(4), matcher.group(3), matcher.group(5));
        }
        Specification<Product> spec = builder.build();
        return new ResponseEntity<>(repository.findAll(spec),HttpStatus.OK);


    }

}
