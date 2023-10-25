//package com.bijlipay.ATFFileGeneration.Config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.config.EnableSpringDataWebSupport;
//import org.springframework.data.domain.Sort.Order;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Configuration
//@EnableSpringDataWebSupport
//public class WebConfig {
//
//    // Custom resolver method for pageable sort direction (.dir) support
//    public Pageable resolvePageable(Map<String, String> requestParams, Pageable pageable) {
//        List<Order> orders = new ArrayList<>();
//        Sort sorts = pageable.getSort();
//        if (sorts != null) {
//            sorts.forEach((sort) -> {
//
//                String propertyName = sort.getProperty();
//                String dirKey = propertyName + ".dir";
//
//                if (requestParams.containsKey(dirKey)) {
//                    String dirValue = requestParams.get(dirKey);
//                    Sort.Direction direction = dirValue.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//                    orders.add(new Sort.Order(direction, propertyName));
//                }
//
//            });
//        }
//        return orders.isEmpty() ? pageable : new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));
//    }
//
//}
