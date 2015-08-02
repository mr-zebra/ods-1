package com.wskj.dao;

import com.wskj.dao.handler.OrderMapper;
import com.wskj.dao.handler.PersonOrderMapper;
import com.wskj.model.Order;
import com.wskj.model.PersonOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuangjy on 2015/7/22.
 */
@Component
public class OrderDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建订单
     *
     * @param orderType
     * @param orderTime
     * @param orderMark
     * @param orderGroup
     */
    public void createOrder(int orderType, Timestamp orderTime, String orderUrl, String orderMark, int orderGroup, Timestamp orderEnd) {
        String sql = "insert into  ods.order(order_type,order_time,order_url,order_mark,order_group,order_end) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, orderType, orderTime, orderUrl, orderMark, orderGroup, orderEnd);
        return;
    }


    public Order getOrder(int orderId) {
        String sql = "select * from ods.order where order_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{orderId}, new OrderMapper());
    }

    public Double getOrderPrice(int orderId){
        String sql = "select order_price from ods.order where order_id = ?";
        return jdbcTemplate.queryForObject(sql,new Object[]{orderId},Double.class);
    }


    //获取最新的小组订单
    public int getLastOrderId(int groupId) {
        String sql = "select order_id from ods.order where order_group=? order by order_id desc";
        return jdbcTemplate.query(sql, new Object[]{groupId}, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getInt("order_id");
            }
        });
    }

    //创建个人订单
    public void createPersonOrder(int orderId, int userId, String orderName, int orderNumber, double orderPrice) {
        String sql = "insert into ods.order_user(order_id,user_id,order_name,order_number,order_price) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, orderId, userId, orderName, orderNumber, orderPrice);
        return;
    }

    /**
     * 更改总表的价格
     *
     * @param orderId
     * @param method  0为增加 1为减去
     * @param num     增加/减去的价格
     */
    public void updateOrderPrice(int orderId, int method, double num) {
        String sql = "select order_price from ods.order where order_id = ?";
        double price = jdbcTemplate.queryForObject(sql, new Object[]{orderId}, Integer.class);
        if (method == 0)
            price += num;
        else
            price -= num;
        sql = "update ods.order set order_price = ? where order_id = ?";
        jdbcTemplate.update(sql, price, orderId);
        return;
    }

    public int getGroupIdFromOrder(int orderId) {
        String sql = "select order_group from ods.order where order_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{orderId}, Integer.class);
    }


    /**
     * 搜索订单
     *
     * @param startTime
     * @param endTime
     * @param url
     * @param startTime
     * @return
     */
    public List<Order> searchOrder(Timestamp startTime, Timestamp endTime, int orderGroup, String url) {
        String sql = "select * from ods.order where order_group=? ";
        if (startTime != null && endTime != null) {
            sql += "and order_time between ? and ? ";
            if (url == "" || url == null) {
                sql += "order by order_id desc";
                return jdbcTemplate.query(sql, new Object[]{orderGroup, startTime, endTime}, new OrderMapper());
            } else {
                sql += "and order_url = ? order by order_id desc";
                return jdbcTemplate.query(sql, new Object[]{orderGroup, startTime, endTime, url}, new OrderMapper());
            }
        } else {
            if (url == "" || url == null) {
                sql += "order by order_id desc";
                return jdbcTemplate.query(sql, new Object[]{orderGroup}, new OrderMapper());
            } else {
                sql += "and order_url = ? order by order_id desc";
                return jdbcTemplate.query(sql, new Object[]{orderGroup, url}, new OrderMapper());
            }
        }
    }

    /**
     * 获取订单的详细信息
     *
     * @param orderId
     * @return
     */
    public List<PersonOrder> getDetailInfo(int orderId) {
        String sql = "select * from ods.order_user where order_id = ?";
        return jdbcTemplate.query(sql, new Object[]{orderId}, new PersonOrderMapper());
    }


    public List<PersonOrder> getLastOrder(int groupId) {
        int orderId = getLastOrderId(groupId);
        Order order = getOrder(orderId);
        if (order.getOrderEnd().getTime() > System.currentTimeMillis()) {
            return getDetailInfo(order.getOrderId());
        } else
            return null;
    }

}
