package com.odde.massivemailer.service.impl;


import com.odde.massivemailer.model.Notification;
import com.odde.massivemailer.model.NotificationDetail;
import com.odde.massivemailer.service.EmailService;
import com.odde.massivemailer.service.NotificationService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class OpenedEmailCounterServiceTest {
    long email_id = 123L;
    SqliteEmail service = new SqliteEmail();
    NotificationServiceSqlite notificationServiceSqlite = new NotificationServiceSqlite();
    EmailCounterTracking emailCounterTracking = new EmailCounterTracking();
    NotificationService notificationService = new NotificationServiceSqlite();

    @Before
    public void setUp() {
        service.destroyAll();
    }

    @Test
    public void shouldReturnEmptyJasonWhenNobodyOpenedTheEmail() {
        Notification notification = new Notification();
        String json = service.extract(notification);
        assertEquals("{\"subject\":\"null\", \"sent_at\":\"null\", \"total_open_count\":0, \"emails\":[]}", json);
    }

    @Test
    public void shouldReturnRecordWithCountWhenOnePersonOpenedTheEmail4Times() {
        Notification notification = createNotification();
        addRecipient(notification, "terry@odd-e.com");
        String json = service.extract(notification);

        assertEquals("{\"subject\":\"test subject\", \"sent_at\":\"2016-11-18\", \"total_open_count\":4, \"emails\":[{\"email\": \"terry@odd-e.com\", \"open_count\": 4}]}", json);
    }

    @Test
    public void shouldReturnRecordWithCountWhenTwoPersonOpenedTheEmail() {
        Notification notification = createNotification();
        addRecipient(notification, "terry@odd-e.com");
        addRecipient(notification, "trump@odd-e.com");

        String json = service.extract(notification);
        assertEquals("{\"subject\":\"test subject\", \"sent_at\":\"2016-11-18\", \"total_open_count\":8, \"emails\":[{\"email\": \"terry@odd-e.com\", \"open_count\": 4}, {\"email\": \"trump@odd-e.com\", \"open_count\": 4}]}", json);
    }

    private Notification createNotification() {
        Notification notification = new Notification();
        notification.setNotificationId(email_id);
        notification.setSubject("test subject");
        String input = "Fri Nov 18 03:19:03 SGT 2016";
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
        TimeZone tz = TimeZone.getTimeZone("Asia/Singapore");
        parser.setTimeZone(tz);
        try {
            notification.setSentDate(parser.parse(input));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return notification;
    }

    private void addRecipient(Notification notification, String emailAddress) {
        NotificationDetail notificationDetail = new NotificationDetail();
        notificationDetail.setEmailAddress(emailAddress);
        notificationDetail.setRead_count(4);
        notification.addNotificationDetail(notificationDetail);
    }
}
