package controllers;

import dao.ReimbursementDao;
import dao.StatusDao;
import dao.TypeDao;
import dao.UserDao;
import io.javalin.http.Context;
import models.JsonResponse;
import models.Reimbursement;
import org.apache.log4j.Logger;
import service.ReimbursementService;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementController {
    public ReimbursementService reimbService = new ReimbursementService(new ReimbursementDao(), new UserDao(), new TypeDao(), new StatusDao());
    Logger log = Logger.getLogger(ReimbursementController.class);

    public ReimbursementController(){


    }

    public void createReimbursementTicket(Context context){
        //required
        int id = 0;
        double amount = 0.00;
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        int author = 0;
        int statusId = 0;
        int typeId = 0;

        try{
            amount = Double.parseDouble(context.formParam("amount"));
            author = Integer.parseInt(context.formParam("author"));
            statusId = Integer.parseInt(context.formParam("statusId"));
            typeId = Integer.parseInt(context.formParam("typeId"));
        }catch (Exception e){
            log.error(e);
        }

        //optional
        String description;
        byte[] reciept;
        File file = null;

        try{
            description = context.formParam("description");

        }catch (Exception e){
            log.info("Description text in reimbursement creation was not present.  If none was desired you can ignore this.");
        }

        Reimbursement ticket = new Reimbursement(id, amount, ts, author, statusId, typeId);
        Boolean successful = reimbService.createNewReimbursementTicket(ticket);

        JsonResponse jsonResponse = new JsonResponse(null, "", successful);

        if(successful){
            context.status(200);
        }else{
            context.status(400);
        }

        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));
    }

    public void getOneTicket(Context context){
        int id = 0;

        try{
            id = Integer.parseInt(context.pathParam("id"));
        }catch (Exception e){

        }

        Reimbursement ticket = reimbService.getOneTicket(id);
        Boolean successful = (ticket != null);
        JsonResponse jsonResponse = new JsonResponse(ticket, "", successful);

        if(successful){
            context.status(200);
        }else{
            jsonResponse.setMessage("Reimbursement ticket with id: " + id + " Not found.");
            context.status(404);
        }

        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));
    }

    public void getAllTickets(Context context){
        int statusId = 0;
        int typeId = 0;
        int userId = 0;
        try{
            statusId = Integer.parseInt(context.formParam("statusId"));
        }catch (Exception e){

        }

        try{
            typeId = Integer.parseInt(context.formParam("typeId"));
        }catch (Exception e){

        }

        try{
            userId = Integer.parseInt(context.formParam("userId"));
        }catch (Exception e){

        }

        List<Reimbursement> tickets;

        if(statusId > 0){
            log.info("Getting all tickets by status id.");
            tickets = reimbService.getAllTicketsByStatus(statusId);
        }else if(typeId > 0){
            log.info("Getting all tickets by type id.");
            tickets = reimbService.getAllTicketsByType(typeId);
        }else if(userId > 0){
            log.info("Getting all tickets by user id.");
            tickets = reimbService.getAllTicketsByUser(userId);
        }else {
            log.info("Getting all tickets.");
            tickets = reimbService.getAllTickets();
        }

        JsonResponse jsonResponse = new JsonResponse(tickets, "", true);


        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));
    }

    //may not need
    public void getAllTicketsByType(Context context){

    }

    //may not need
    public void getAllTicketsByStatus(Context context){

    }

    public void deleteTicket(Context context){
        int ticketId = 0;
        JsonResponse jsonResponse = new JsonResponse(null, "", false);

        try{
            ticketId = Integer.parseInt(context.pathParam("id"));
        }catch (Exception e){
            jsonResponse.setMessage("Invalid number/symbol in url.");
            log.error(e);
        }

        Boolean deleted = reimbService.deleteTicket(ticketId);
        jsonResponse.setSuccessful(deleted);

        if(deleted){
            context.status(200);
        }else{
            context.status(400);
        }

        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));
    }

    public void updateTicket(Context context){
        //required
        int id = 0;
        double amount = 0.00;
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        int author = 0;
        int statusId = 0;
        int typeId = 0;

        try{
            id = Integer.parseInt(context.pathParam("id"));
        }catch (Exception e){
            log.error(e);
        }
        try{
            amount = Double.parseDouble(context.formParam("amount"));
        }catch (Exception e){
            log.error(e);
        }
        try{
            author = Integer.parseInt(context.pathParam("id"));
        }catch (Exception e){
            log.error(e);
        }
        try{
            statusId = Integer.parseInt(context.formParam("statusId"));
        }catch (Exception e){
            log.error(e);
        }
        try{
            typeId = Integer.parseInt(context.formParam("typeId"));
        }catch (Exception e){
            log.error(e);
        }

        //optional
        String description;
        byte[] reciept;
        File file = null;

        try{
            description = context.formParam("description");

        }catch (Exception e){
            log.info("Description text in reimbursement creation was not present.  If none was desired you can ignore this.");
        }

        Reimbursement updatedTicket = new Reimbursement(id, amount, ts, author, statusId, typeId);
        updatedTicket = reimbService.updateTicket(updatedTicket);

        JsonResponse jsonResponse = new JsonResponse(updatedTicket, "", true);


        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));

    }

    public void resolveTicket(Context context){
        //checks to see if the parameter exists, what it contains is irrelevant
        Boolean approved = context.formParam("approved") != null;
        int ticketId = 0;
        int resolverId = 0;
        String message = "";
        try{
            ticketId = Integer.parseInt(context.pathParam("id"));
            resolverId = Integer.parseInt(context.formParam("resolverId"));
        }catch (Exception e){
            log.error(e);
        }

        if(approved){
            approved = reimbService.approveTicket(ticketId, resolverId);
            message = (approved)?"Ticket Approved.":"Error approving ticket.";
        }else{
            approved = reimbService.denyTicket(ticketId, resolverId);
            message = (approved)?"Ticket Denied.":"Error denying ticket.";

        }

        JsonResponse jsonResponse = new JsonResponse(null, message, approved);

        context.contentType("application/json");
        context.result(JsonConverter.convertToJson(jsonResponse));
    }

    //may not need
    public void denyTicket(Context context){

    }
}
