import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import org.hibernate.*;
import org.hibernate.cfg.*;
import javax.persistence.*;
import java.util.*;

@Entity
class Student {
    @Id
    private int id;
    private String name;
    private String course;
    public Student() {}
    public Student(int id, String name, String course) {
        this.id = id; this.name = name; this.course = course;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCourse() { return course; }
    public void setName(String name) { this.name = name; }
    public String toString() { return id + " " + name + " " + course; }
}

@Entity
class Account {
    @Id
    private int accId;
    private String holder;
    private double balance;
    public Account() {}
    public Account(int accId, String holder, double balance) {
        this.accId = accId; this.holder = holder; this.balance = balance;
    }
    public int getAccId() { return accId; }
    public double getBalance() { return balance; }
    public void setBalance(double b) { this.balance = b; }
}

@Entity
class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tid;
    private int fromAcc;
    private int toAcc;
    private double amount;
    private Date date;
    public TransactionRecord() {}
    public TransactionRecord(int fromAcc, int toAcc, double amount) {
        this.fromAcc = fromAcc; this.toAcc = toAcc; this.amount = amount; this.date = new Date();
    }
}

class Course {
    private String title;
    public Course(String title) { this.title = title; }
    public String getTitle() { return title; }
}

class StudentDI {
    private Course course;
    public StudentDI(Course course) { this.course = course; }
    public void showInfo() { System.out.println("Student enrolled in: " + course.getTitle()); }
}

@Configuration
@ComponentScan(basePackages = "com.example")
@EnableTransactionManagement
class AppConfig {
    @Bean public Course course() { return new Course("Spring and Hibernate"); }
    @Bean public StudentDI studentDI() { return new StudentDI(course()); }
    @Bean public SessionFactory sessionFactory() {
        return new Configuration().configure("hibernate.cfg.xml").addAnnotatedClass(Student.class).addAnnotatedClass(Account.class).addAnnotatedClass(TransactionRecord.class).buildSessionFactory();
    }
    @Bean public HibernateDAO dao() { return new HibernateDAO(); }
    @Bean public BankService bankService() { return new BankService(); }
}

@Repository
class HibernateDAO {
    @Autowired private SessionFactory factory;
    public void addStudent(Student s) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(s); tx.commit(); session.close();
    }
    public Student getStudent(int id) {
        Session session = factory.openSession();
        Student s = session.get(Student.class, id);
        session.close(); return s;
    }
    public void updateStudent(int id, String name) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student s = session.get(Student.class, id);
        s.setName(name);
        session.update(s);
        tx.commit(); session.close();
    }
    public void deleteStudent(int id) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student s = session.get(Student.class, id);
        session.delete(s);
        tx.commit(); session.close();
    }
    public void updateAccount(Account a) {
        Session session = factory.getCurrentSession();
        session.update(a);
    }
    public Account getAccount(int id) {
        Session session = factory.getCurrentSession();
        return session.get(Account.class, id);
    }
    public void saveTransaction(TransactionRecord tr) {
        Session session = factory.getCurrentSession();
        session.save(tr);
    }
}

@Service
class BankService {
    @Autowired private HibernateDAO dao;
    @Transactional
    public void transferMoney(int from, int to, double amount) {
        Account a1 = dao.getAccount(from);
        Account a2 = dao.getAccount(to);
        if (a1.getBalance() < amount) throw new RuntimeException("Insufficient Balance");
        a1.setBalance(a1.getBalance() - amount);
        a2.setBalance(a2.getBalance() + amount);
        dao.updateAccount(a1);
        dao.updateAccount(a2);
        dao.saveTransaction(new TransactionRecord(from, to, amount));
    }
}

public class SpringHibernateApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        StudentDI sdi = context.getBean(StudentDI.class);
        sdi.showInfo();

        HibernateDAO dao = context.getBean(HibernateDAO.class);
        Student s1 = new Student(1, "John", "Spring Boot");
        dao.addStudent(s1);
        System.out.println("Added: " + dao.getStudent(1));
        dao.updateStudent(1, "Johnny");
        System.out.println("Updated: " + dao.getStudent(1));
        dao.deleteStudent(1);
        System.out.println("Deleted student with ID 1");

        SessionFactory factory = context.getBean(SessionFactory.class);
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(new Account(101, "Alice", 5000));
        session.save(new Account(102, "Bob", 3000));
        tx.commit(); session.close();

        BankService bank = context.getBean(BankService.class);
        try {
            bank.transferMoney(101, 102, 1000);
            System.out.println("Transaction successful");
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        context.close();
    }
}
