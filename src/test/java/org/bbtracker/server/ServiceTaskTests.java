package org.bbtracker.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.kickmyb.server.ServerApplication;
import org.kickmyb.server.account.BadCredentialsException;
import org.kickmyb.server.account.MUser;
import org.kickmyb.server.account.MUserRepository;
import org.kickmyb.server.account.ServiceAccount;
import org.kickmyb.server.task.MTask;
import org.kickmyb.server.task.MTaskRepository;
import org.kickmyb.server.task.ServiceTask;
import org.kickmyb.transfer.AddTaskRequest;
import org.kickmyb.transfer.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;

// TODO pour celui ci on aimerait pouvoir mocker l'utilisateur pour ne pas avoir à le créer

// https://reflectoring.io/spring-boot-mock/#:~:text=This%20is%20easily%20done%20by,our%20controller%20can%20use%20it.

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = ServerApplication.class)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServiceTaskTests {

    @Autowired
    private MUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceTask serviceTask;
    @Autowired
    private MTaskRepository taskRepository;




    @Test
    @Transactional
    void testHardDeleteTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        //create user
        MUser u = createUser("M. Test");

        //create task
        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));


        //add task
        serviceTask.addOne(atr, u);
        MTask task = taskRepository.findAll().iterator().next();
        //confirm it was added
        Assertions.assertTrue(taskRepository.findById(task.id).isPresent());
        //apply delete
        try {
            serviceTask.hardDelete(task.id, u);//try catch w error catching
        } catch (ServiceTask.TaskNotFound e) {
            fail("echec du test , tache introuvable.");
        } catch (ServiceTask.UnauthorizedAccess e) {
            fail("echec du test , acces non autoriser");
        }

        boolean taskExists = taskRepository.findById(task.id).isPresent();
        Assertions.assertFalse(taskExists, "La tâche n'a pas été supprimée correctement.");
    }

    @Test
    void testAddTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        //create user
        MUser u = new MUser();
        u.username = "testerino";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);
        //ajt req for task
        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);

        Assertions.assertEquals(1, serviceTask.home(u.id).size());
    }

    @Test
    void testAddTaskEmpty() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Empty");
        } catch (Exception e) {
            assertEquals(ServiceTask.Empty.class, e.getClass());
        }
    }

    @Test
    void testAddTaskTooShort() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "o";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.TooShort");
        } catch (Exception e) {
            assertEquals(ServiceTask.TooShort.class, e.getClass());
        }
    }

    @Test
    void testAddTaskExisting() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }






    private MUser createUser(String username) {
        MUser user = new MUser();
        user.username = username;
        user.password = passwordEncoder.encode("Passw0rd!");
        userRepository.save(user);
        return user;
    }

}
