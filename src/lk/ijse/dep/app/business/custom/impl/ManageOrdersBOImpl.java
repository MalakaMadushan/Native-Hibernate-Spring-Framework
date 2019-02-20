package lk.ijse.dep.app.business.custom.impl;

import lk.ijse.dep.app.business.Converter;
import lk.ijse.dep.app.business.custom.ManageOrdersBO;
import lk.ijse.dep.app.dao.custom.*;
import lk.ijse.dep.app.dto.*;
import lk.ijse.dep.app.entity.CustomEntity;
import lk.ijse.dep.app.entity.Item;
import lk.ijse.dep.app.entity.Order;
import lk.ijse.dep.app.entity.OrderDetail;
import lk.ijse.dep.app.util.HibernateUtil;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
public class ManageOrdersBOImpl implements ManageOrdersBO {
    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private ItemDAO itemDAO;
    private QueryDAO queryDAO;
    private CustomerDAO customerDAO;


    @Autowired
    public ManageOrdersBOImpl(OrderDAO orderDAO) {
        this.orderDAO=orderDAO;
        this.queryDAO= queryDAO;
       // orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
       // orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
        //itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
       // queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
       // customerDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.CUSTOMER);

    }

    @Override
    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {

        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            queryDAO.setSession(session);

            session.beginTransaction();
//             List<OrderDTO2> orderDTO2s = queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
//                return Converter.getDTOList(ce, OrderDTO2.class);
//            }).get();
            List<CustomEntity> alt = queryDAO.findAllOrdersWithCustomerNameAndTotal();

            ArrayList<OrderDTO2> objects = new ArrayList<>();
            for (CustomEntity customEntity : alt) {
                objects.add(new OrderDTO2(customEntity.getOrderId(), customEntity.getOrderDate().toLocalDate(), customEntity.getCustomerId(), customEntity.getCustomerName(), customEntity.getTotal()));
            }

            session.getTransaction().commit();
            return objects;
        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public List<OrderDTO> getOrders() throws Exception {

        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            orderDAO.setSession(session);

            session.beginTransaction();
            List<OrderDTO> orderDTOS = orderDAO.findAll().map(Converter::<OrderDTO>getDTOList).get();
            session.getTransaction().commit();
            return orderDTOS;
        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public String generateOrderId() throws Exception {
        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            orderDAO.setSession(session);

            session.beginTransaction();
            String count = orderDAO.count() + 1 + "";
            session.getTransaction().commit();
            System.out.println(count);
            return count;
        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }

    }

    @Override
    public void createOrder(OrderDTO dto) throws Exception {
        Session mySession = HibernateUtil.getSessionFactory().openSession();
        boolean result = false;
        try (Session session = mySession) {
            orderDAO.setSession(session);
            session.beginTransaction();
            orderDAO.save(new Order(dto.getId(), Date.valueOf(dto.getDate()), Converter.getEntity(dto.getCustomer())));
            result = true;
            if (result) {
                orderDetailDAO.setSession(session);

                for (OrderDetailDTO detailDTO : dto.getOrderDetailDTOS()) {
                    orderDetailDAO.save(new OrderDetail(dto.getId(),
                            detailDTO.getCode(), detailDTO.getQty(), detailDTO.getUnitPrice()));


                    itemDAO.setSession(session);
                    Item item = itemDAO.find(detailDTO.getCode()).get();
                    int qty = item.getQtyOnHand() - detailDTO.getQty();
                    item.setQtyOnHand(qty);
                    itemDAO.update(item);

                }
            }

            session.getTransaction().commit();
        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public OrderDTO findOrder(String orderId) throws Exception {
        CustomerDTO customerDTO ;
//        Session mySession1 = HibernateUtil.getSessionFactory().openSession();
//        try(Session session = mySession1){
//            customerDAO.setSession(session);
//            session.beginTransaction();
//            customerDTO = customerDAO.find("c001").map(Converter::<CustomerDTO>getDTO).orElse(null);
//            session.getTransaction().commit();
//
//        }catch(Exception ex){
//            System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
//            mySession1.getTransaction().rollback();
//            throw ex;
//        }
////////////////////////////////////////////////////////////////////////////////////
        List<OrderDetailDTO> dtoList = null;
//        Session mySession2 = HibernateUtil.getSessionFactory().openSession();
//        try (Session session = mySession2) {
//            orderDetailDAO.setSession(session);
//            session.beginTransaction();
//
//
//
//            List<OrderDetail> orderDetails = orderDetailDAO.find(orderId);
//            for (OrderDetail orderDetail : orderDetails) {
//                dtoList.add(new OrderDetailDTO(orderDetail.getOrder().getId(), orderDetail.getCode().getDescription(), orderDetail.getQty(), orderDetail.getUnitPrice()));
//            }
////             dtoList = Converter.getDTOList(orderDetails);
//
//
//
//            session.getTransaction().commit();
//
//        } catch (Exception ex) {
//            System.out.println("sssssssssssssssssssssssssssssss");
//            mySession2.getTransaction().rollback();
//            throw ex;
//
//        }

///////////////////////////////////////////////////////////////////////////////////////
        boolean result=false;
        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            queryDAO.setSession(session);
            session.beginTransaction();
            List<CustomEntity> odwtid = queryDAO.findOrderDetailsWithItemDescriptions(orderId);
            OrderDTO orderDTO = null;
            result=true;
            if(result) {


                customerDAO.setSession(session);
                customerDTO = customerDAO.find("c001").map(Converter::<CustomerDTO>getDTO).orElse(null);
                result=true;
                if(result) {
                    orderDetailDAO.setSession(session);
                    List<OrderDetail> orderDetails = orderDetailDAO.find(orderId);
                    for (OrderDetail orderDetail : orderDetails) {
                        dtoList.add(new OrderDetailDTO(orderDetail.getOrder().getId(), orderDetail.getCode().getDescription(), orderDetail.getQty(), orderDetail.getUnitPrice()));
                    }

                            for (CustomEntity customEntity : odwtid) {

                                orderDTO = new OrderDTO(customEntity.getOrderId(), customEntity.getOrderDate().toLocalDate(), customerDTO, dtoList);
                            }

                }else {
                    mySession.getTransaction().rollback();
                }
            }else {
                mySession.getTransaction().rollback();
            }
            session.getTransaction().commit();
            return orderDTO;
        } catch (Exception ex) {
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            mySession.getTransaction().rollback();
            throw ex;
        }
    }
}