package com.bijlipay.ATFFileGeneration.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class BaseController {


    private final Logger logger = LoggerFactory.getLogger(BaseController.class);
//    @Value("${switch.datasource.url}")
//    private String switchUrl;
//    @Value("${switch.datasource.username}")
//    private String username;
//    @Value("${switch.datasource.password}")
//    private String password;

//    public Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(switchUrl, username, password);
//    }


    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username1;
    @Value("${spring.datasource.password}")
    private String password1;

    public Connection getConnection1() throws SQLException {
        return DriverManager.getConnection(url, username1, password1);
    }
}
