package org.kimios.tests.kernel;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kimios.client.controller.helpers.StringTools;
import org.kimios.kernel.security.model.Session;
import org.kimios.kernel.user.model.User;
import org.kimios.tests.TestAbstract;
import org.kimios.tests.deployments.OsgiDeployment;
import org.kimios.tests.utils.dataset.Users;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by tom on 11/02/16.
 */
@RunWith(Arquillian.class)
public class KernelTestAbstractTest extends KernelTestAbstract {

    @Deployment(name="karaf")
    public static JavaArchive createDeployment() {
        String jarName = "KernelTestAbstractTest.jar";
        return OsgiDeployment.createArchive(jarName, null, KernelTestAbstractTest.class,
                KernelTestAbstract.class,
                StringTools.class);
    }

    @Before
    public void setUp() {
        this.init();

//        // Get the service reference
//        ServiceReference<ISecurityController> sref = context.getServiceReference(ISecurityController.class);
//        // Get the service
//        this.getSecurityController() = context.getService(sref);
//
//        ServiceReference<IAdministrationController> srefAdmin = context.getServiceReference(IAdministrationController.class);
//        this.administrationController = context.getService(srefAdmin);
////        this.init();
//
//        ServiceReference<IWorkspaceController> sRefWorkspace = context.getServiceReference(IWorkspaceController.class);
//        this.workspaceController = context.getService(sRefWorkspace);

        this.setAdminSession(this.getSecurityController().startSession(TestAbstract.ADMIN_LOGIN, Users.USER_TEST_SOURCE, TestAbstract.ADMIN_PWD));

        this.createWorkspaceTestIfNotExists();
        this.workspaceTest = this.workspaceController.getWorkspace(this.getAdminSession(), WORKSPACE_TEST_NAME);

        this.createUserTestIfNotExists(DEFAULT_USER_TEST_ID);

    }

    @After
    public void tearDown() {
        this.deleteUserTest();
    }

    @Test
    public void testChangePermissionOnEntityForUser() {
        assertTrue(this.getSecurityController().isSessionAlive(this.getAdminSession().getUid()));
        User userTest = this.administrationController.getUser(this.getAdminSession(), DEFAULT_USER_TEST_ID, Users.USER_TEST_SOURCE);
        Session userTestSession = this.getSecurityController().startSession(userTest.getUid(), Users.USER_TEST_SOURCE, DEFAULT_USER_TEST_PASS);
        assertFalse(this.getSecurityController().canRead(userTestSession, this.workspaceTest.getUid()));
    }

    @Test
    public void testCreateUserFromPojoWithPassword() {
        org.kimios.kernel.ws.pojo.User user = new org.kimios.kernel.ws.pojo.User(
                "userTestFromPojo1",
                "Johnny",
                "Cash",
                "06060606060",
                Users.USER_TEST_SOURCE,
                new Date(),
                "mail"
        );

        this.createUserFromPojoWithPassword(user, "test");
        // user exists ?
        User userExtracted = this.administrationController.getUser(this.getAdminSession(), "userTestFromPojo1", Users.USER_TEST_SOURCE);
        assertNotNull(userExtracted);
        assertEquals("Johnny", user.getFirstName());
        assertEquals("Cash", user.getLastName());
        assertEquals("06060606060", user.getPhoneNumber());
        assertEquals("mail", user.getMail());

        // user can connect ?
        Session session = this.getSecurityController().startSession("userTestFromPojo1", Users.USER_TEST_SOURCE, "test");
        assertNotNull(session);

        // some cleaning
        this.administrationController.deleteUser(this.getAdminSession(), "userTestFromPojo1", Users.USER_TEST_SOURCE);
    }
}
