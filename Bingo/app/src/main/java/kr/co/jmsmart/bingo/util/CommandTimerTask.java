package kr.co.jmsmart.bingo.util;

import java.util.TimerTask;

import kr.co.jmsmart.bingo.data.Command;

/**
 * Created by Administrator on 2019-02-25.
 */

public abstract class CommandTimerTask extends TimerTask {
    private Command cmd;

    public CommandTimerTask(Command cmd) {
        this.cmd = cmd;
    }

    public Command getCmd() {
        return cmd;
    }

    public void setCmd(Command cmd) {
        this.cmd = cmd;
    }
}
