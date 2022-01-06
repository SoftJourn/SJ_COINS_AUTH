package com.softjourn.coin.auth.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LoginFilter implements Filter {

  @Bean
  public FilterRegistrationBean loginFilterRegistrationBean() {
    FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
    filterRegistration.setFilter(this);
    filterRegistration.addUrlPatterns("/oauth/token");
    return filterRegistration;
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(
      ServletRequest request, ServletResponse response, FilterChain chain
  ) throws IOException, ServletException {
    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {

      @Override
      public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>(super.getParameterMap());
        map.computeIfPresent("username",
            (k, v) -> Arrays.stream(v)
                .map(s -> s.trim().toLowerCase())
                .toArray(String[]::new));
        return map;
      }
    };

    chain.doFilter(wrapper, response);
  }

  @Override
  public void destroy() {
  }
}
