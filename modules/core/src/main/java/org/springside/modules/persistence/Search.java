package org.springside.modules.persistence;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springside.modules.web.Servlets;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Search {

    public final static int DEAULT_PAGE_SIZE  = 10;


    public static Map<String, SearchFilter> parse(ServletRequest request, String prefix) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, prefix);
        return SearchFilter.parse(searchParams);
    }

    public static void addFilter(Map<String, SearchFilter> filters, String fieldName, SearchFilter.Operator operator, Object value){
        filters.put(new String(fieldName),new SearchFilter(fieldName,operator,value));
    }

    public static <T> Specification<T> parse(ServletRequest request, String prefix, Class<T> entityClazz) {
        Map<String, SearchFilter> filters = parse(request, prefix);
        return DynamicSpecifications.bySearchFilter(filters.values(),entityClazz);
    }

    public static PageRequest page(ServletRequest request, String orderPrefix, Sort defaultSort) {
        String _pageSize = request.getParameter("numPerPage");//dwz参数
        int pageSize=DEAULT_PAGE_SIZE;
        if(StringUtils.isNotBlank(_pageSize)){
            pageSize=Integer.valueOf(_pageSize);
        }
        String _pageNumber = request.getParameter("pageNum");//dwz参数
        int pageNumber=0;
        if(StringUtils.isNotBlank(_pageNumber)){
            pageNumber=Integer.valueOf(_pageNumber)-1;
        }

        Map<String, Object> orders = Servlets.getParametersStartingWith(request, orderPrefix);
        List<Sort.Order> orderList=new ArrayList<>();

        if(orders!=null&&!orders.isEmpty()){
            orders.forEach((k,v)->{
                //order_0_biz orderList.add(0,order)
                //order_1_status orderList.add(1,order)
                Integer idx=null;
                if(k.contains("_")){
                    String[] split = k.split("_");
                    k=split[1];
                    idx=Integer.valueOf(split[0]);
                }
                Sort.Order order = new Sort.Order(Sort.Direction.fromString(v.toString()),k);
                if (idx!=null){
                    orderList.add(idx,order);
                }else {
                    orderList.add(order);
                }
            });
        }
        if(!orderList.isEmpty()){
            return new PageRequest(pageNumber,pageSize, new Sort(orderList));
        }
        if(defaultSort!=null){
            return new PageRequest(pageNumber,pageSize,defaultSort);
        }
        return new PageRequest(pageNumber,pageSize);
    }
}
