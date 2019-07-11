package com.aazizova.springboottesttask.controller;

import com.aazizova.springboottesttask.model.entity.Product;
import com.aazizova.springboottesttask.service.ProductService;
import com.aazizova.springboottesttask.utils.ProductUtils;
import com.aazizova.springboottesttask.utils.builder.CustomEntityBuilder;
import com.google.code.siren4j.component.Entity;
import com.google.code.siren4j.converter.ReflectingConverter;
import com.google.code.siren4j.error.Siren4JException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Log4j2
//@CrossOrigin(origins = "http://localhost:3000")
@Api(value = "Simple Inventory System",
        description = "Operations for products in Simple Inventory System")
public class ProductController {
    @Autowired
    ProductService productService;

    @Autowired
    ProductUtils productUtils;

    @Autowired
    CustomEntityBuilder customEntityBuilder;

    /**
     * Returns entity of all products.
     *
     * @param req HttpServletRequest
     *
     * @throws Siren4JException if something with siren format happened
     *
     * @return Entity
     */
    @ApiOperation(value = "View all products", response = Entity.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpServletResponse.SC_OK,
                    message = "Successfully retrieved products"),
            @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT,
                    message = "There are no products"),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED,
                    message = "You are not authorized to view the resource"),
            @ApiResponse(code = HttpServletResponse.SC_FORBIDDEN,
                    message = "Access is forbidden")
    })
    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public Entity products(final HttpServletRequest req)
            throws Siren4JException {
        log.info("Getting products");
        List<Product> products = productService.retrieveProducts();
        if (products.isEmpty()) {
            log.info("There are no products");
            return customEntityBuilder.errorEntity(HttpStatus.NO_CONTENT,
                    "There are no products");
        }
        return customEntityBuilder.productsEntity(products, req, "products");
    }

    /**
     * Returns entity of product.
     *
     * @param id id of product
     *
     * @throws Siren4JException if something with siren format happened
     *
     * @return Entity
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public Entity product(final @PathVariable(name = "id") Long id)
            throws Siren4JException { //TODO add exception handling
        log.info("Getting Product with id = [" + id + "]");
        Product product = productService.productWithId(id);
        if (product == null) {
            log.info("Product with id = [" + id + "] not found");
            return customEntityBuilder.errorEntity(HttpStatus.NOT_FOUND,
                    "Product with id = [" + id + "] not found");
        }
        return ReflectingConverter.newInstance().toEntity(product);
    }

    @PostMapping("/")
    @Secured("ROLE_ADMIN")
    public Entity addProduct(final @RequestBody Product product)
            throws Siren4JException {
        log.info("Saving Product = [" + product + "]");
        productService.addProduct(product);
        return ReflectingConverter.newInstance().toEntity(product);
    }

    /**
     * Returns entity of deleted product.
     *
     * @param id id of product
     *
     * @return Entity
     */
    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public Entity deleteProduct(final @PathVariable(name = "id") Long id) {
        log.info("Deleting Product with id = [" + id + "]");
        Product product = productService.productWithId(id);
        if (product == null) {
            log.info("Unable to delete product with id = [" + id + "]"
                    + " because it's not found");
            return customEntityBuilder.errorEntity(HttpStatus.NOT_FOUND,
                    "Unable to delete product with id = [" + id + "]"
                    + " because it's not found");
        }
        productService.deleteProductById(id);
        return customEntityBuilder.successEntity();
    }

    /**
     * Returns entity of updated product. //TODO check all comments like this
     *
     * @param product product
     * @param id id of product
     *
     * @return Entity
     */
    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public Entity updateProduct(final @RequestBody Product product,
                                final @PathVariable(name = "id") Long id) {
        log.info("Updating Product =[" + product + "]");
        Product prod = productService.productWithId(id);
        if (prod == null) {
            log.info("Product with id = [" + id + "] not found");
            return customEntityBuilder.errorEntity(HttpStatus.NOT_FOUND,
                    "Product with id = [" + id + "] not found");
        }
        productService.updateProduct(product);
        return customEntityBuilder.successEntity();
    }

    @PostMapping("/export")
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public Entity exportProducts(@RequestBody List<Product> products) {
        log.info("products = [" + products + "]");
        log.info("Exporting filtered products");
        if (!productUtils.exportToXLS(products)) {
            log.info("Can't export");
            return customEntityBuilder.errorEntity(HttpStatus.NO_CONTENT,
                    "Can't export");
        }
        return customEntityBuilder.successEntity();
    }

    @GetMapping("/leftovers")
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public Entity leftovers(final HttpServletRequest request) {
        log.info("Getting leftovers");
        List<Product> leftovers = productService.retrieveLeftovers();
        if (leftovers.isEmpty()) {
            log.info("There are no leftovers");
            return customEntityBuilder.errorEntity(HttpStatus.NO_CONTENT,
                    "There are no leftovers");
        }
        return customEntityBuilder.productsEntity(leftovers,
                request, "leftovers");
    }
}
