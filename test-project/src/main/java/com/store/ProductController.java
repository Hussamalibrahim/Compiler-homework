package com.store;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private List<Product> productList = new ArrayList<>();

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productList);
        return "products/list";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/add";
    }

    @PostMapping("/products/add")
    public String addProduct(Product product) {
        return "redirect:/products";
    }

    @GetMapping("/products/details/{id}")
    public String viewProductDetails(@PathVariable Long id, Model model) {
        Product found = new Product(id, "Laptop", 999.99, "High performance laptop", "laptop.png");
        model.addAttribute("product", found);
        return "products/details";
    }

    @GetMapping("/products/test-error")
    public String errorEndpoint(Model model) {
        model.addAttribute("unusedInfo", "Some debug message");
        return "products/missing_template";
    }
}
