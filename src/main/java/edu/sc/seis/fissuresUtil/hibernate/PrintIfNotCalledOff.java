package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Timer;
import java.util.TimerTask;

class PrintIfNotCalledOff extends TimerTask {

    PrintIfNotCalledOff(String msg) {
        this(msg, 5);
    }

    PrintIfNotCalledOff(String msg, int delaySeconds) {
        this.msg = msg;
        Timer t = new Timer();
        t.schedule(this, delaySeconds * 1000);
    }

    public void callOff() {
        calledOff = true;
    }

    public void run() {
        if (!calledOff) {
            logger.info(msg);
        }
    }

    String msg;

    boolean calledOff = false;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractHibernateDB.class);
}