package org.copydays.projects.user.web.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CharsetEncodingFilter implements Filter {

    private String encoding = null;

    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.encoding = filterConfig.getInitParameter("encoding");
        this.servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 只需要判断 request 就可以断定是 HTT篇，response 就直接转了
        if (request instanceof HttpServletRequest){
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            httpRequest.setCharacterEncoding(encoding);
            httpResponse.setCharacterEncoding(encoding);

            // 执行顺序：CharsetEncodingFilter -> FrontControllerServlet -> forward -> index.jsp
            servletContext.log("设置当前编码为：" + encoding);
        }

        // 执行过滤器链
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
