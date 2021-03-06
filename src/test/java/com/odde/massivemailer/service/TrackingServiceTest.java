package com.odde.massivemailer.service;

import com.odde.massivemailer.model.Notification;
import com.odde.massivemailer.model.NotificationDetail;
import com.odde.massivemailer.service.impl.NotificationServiceSqlite;
import com.odde.massivemailer.service.impl.SqliteTracking;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TrackingServiceTest {
    private TrackingService service;

    private NotificationService notificationService;

    @Before
    public void setUp() {
        service = new SqliteTracking();
        notificationService = new NotificationServiceSqlite();
    }

    @Test
    public void UpdateViewCountMustIncrementByOne() {
        Notification notification = new Notification();
        notification.setSubject("Subject");
        notification.setNotificationId(1234567890L);

        NotificationDetail notificationDetail = new NotificationDetail();
        notificationDetail.setEmailAddress("terry@odd-e.com");
        notification.addNotificationDetail(notificationDetail);

        Notification savedNotification = notificationService.save(notification);
        NotificationDetail savedNotificationDetail = savedNotification.getNotificationDetails().get(0);

        int count = service.updateViewCount(savedNotificationDetail.getId());

        assertThat(count, is(1));
    }

    @Test
    public void UpdateViewCountMustReturnZeroOnError() {
        int count = service.updateViewCount(-1L);

        assertThat(count, is(0));
    }
}
