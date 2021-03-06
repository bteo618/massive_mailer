package com.odde.massivemailer.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.odde.massivemailer.exception.EmailException;
import com.odde.massivemailer.model.ContactPerson;
import com.odde.massivemailer.model.Mail;
import com.odde.massivemailer.model.Notification;
import com.odde.massivemailer.service.NotificationService;
import com.odde.massivemailer.service.impl.GMailService;
import com.odde.massivemailer.service.impl.NotificationServiceSqlite;
import com.odde.massivemailer.service.impl.SMTPConfiguration;

import com.odde.massivemailer.service.impl.SqliteContact;

public class SendMailController extends HttpServlet {

    private SqliteContact sqliteContact;

    private static final String SMTP_ADDR = "smtp.gmail.com";
    private static final int PORT = 587;
    public static final String EMAIL_USERID = "MM_EMAIL_USERID";
    public static final String EMAIL_PASSWORD = "MM_EMAIL_PASSWORD";

    private GMailService gmailService;
    private NotificationService notificationService;

    public SendMailController() {
        notificationService = new NotificationServiceSqlite();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Mail email = processRequest(req);

            Notification notification = notificationService.save(email.asNotification());
            email.setNotification(notification);

            GMailService mailService = createGmailService();
            mailService.send(email);

            resp.sendRedirect("sendemail.jsp?status=success&msg=Email successfully sent&repcnt=" + email.getReceipts().size());
        } catch (EmailException e) {
            resp.sendRedirect("sendemail.jsp?status=failed&msg=Unable to send");
            e.printStackTrace();
        } catch (SQLException e) {
            resp.sendRedirect("sendemail.jsp?status=failed&msg=Fail");
            e.printStackTrace();
        }
    }

    private GMailService createGmailService() {
        if (null == gmailService) {
            SMTPConfiguration config = new SMTPConfiguration(System.getenv(EMAIL_USERID), System.getenv(EMAIL_PASSWORD), SMTP_ADDR, PORT);
            gmailService = new GMailService(config);
        }

        return gmailService;
    }

    public Mail processRequest(HttpServletRequest req) throws SQLException {

        Mail email = new Mail();
        String tempRecipient = req.getParameter("recipient");
        StringTokenizer st = new StringTokenizer(tempRecipient, ";");
        ArrayList<String> recipientList = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String recipient = st.nextToken();
            if (recipient.startsWith("company:")) {

                String[] aaa = recipient.split(":");
                String company = aaa[1].toString();
                List<ContactPerson> contactList = getContactPersons(company);
                if (contactList.isEmpty()) {
                    throw new SQLException();
                }
                for (ContactPerson contactPerson : contactList) {
                    recipientList.add(contactPerson.getEmail());
                }
            } else {
                recipientList.add(recipient);
            }
        }
        email.setMessageId(System.currentTimeMillis());
        email.setContent(req.getParameter("content"));
        email.setSubject(req.getParameter("subject"));

        email.setReceipts(recipientList);

        return email;
    }

    private List<ContactPerson> getContactPersons(String company) throws SQLException {
        List<ContactPerson> contactList;

        company = company.replaceAll("\"", "");

        SqliteContact contactService = getSqliteContact();
        contactList = contactService.getContactListFromCompany(company);
        return contactList;
    }

    public SqliteContact getSqliteContact() {
        return (sqliteContact == null) ? sqliteContact = new SqliteContact() : sqliteContact;
    }

    public void setSqliteContact(SqliteContact contactService) {
        sqliteContact = contactService;
    }

    public void setGmailService(GMailService gmailService) {
        this.gmailService = gmailService;
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
