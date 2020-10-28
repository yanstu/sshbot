package cn.yanstu.sshbot.listener.privatemsg;

import ch.ethz.ssh2.Connection;
import cn.yanstu.sshbot.util.RemoteCommandUtil;
import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.anno.depend.Beans;
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * @author luckyx
 * 消息监听器
 */
@Beans
public class MasterListener {

    Connection conn;
    private final Integer maxSize = 800;
    private final String master = "xxxxx";
    private final String ip = "xxxxxxxxxx";
    private final String userName = "root";
    private final String userPwd = "xxxxxxxx";
    private final String[] keywords = {"开启连接", "关闭连接"};

    @Listen(MsgGetTypes.privateMsg)
    public void verification(PrivateMsg priMsg, MsgSender sender) {
        if (!priMsg.getCode().equals(master)) {
            sender.SENDER.sendPrivateMsg(priMsg, "你没有权限哦");
        }
        ;
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(code = master)
    public void privateMsg(PrivateMsg priMsg, MsgSender sender) {
        // thisCode 代表当前接收到消息的机器人账号。
        String botCode = priMsg.getThisCode();
        // 发消息人的昵称
        String nickname = priMsg.getNickname();
        // 发消息人的账号
        String code = priMsg.getCode();
        // 发消息人发的消息
        String msg = priMsg.getMsg();
        if (!Arrays.asList(keywords).contains(msg)) {
            MessageFormat message = new MessageFormat("机器人{0}接收到了{1}({2})的指令消息：{3}");
            String printMsg = message.format(new Object[]{botCode, nickname, code, msg});
            System.out.println(printMsg);
            if (conn == null) {
                conn = RemoteCommandUtil.login(ip, userName, userPwd);
            }
            String result = RemoteCommandUtil.execute(conn, msg);
            try {
                sender.SENDER.sendPrivateMsg(code, result);
                System.out.println(result);
            } catch (IllegalStateException exception) {
                int ss = result.length() % maxSize == 0 ? result.length() / maxSize : result.length() / maxSize + 1;
                sender.SENDER.sendPrivateMsg(priMsg, "因发送最大字符数为800，所以拆分为" + ss + "次发送，总字符数为：" + result.length());
                for (int i = 1; i <= ss; i++) {
                    String str = result.substring(maxSize * (i - 1), Math.min(maxSize * i, result.length()));
                    sender.SENDER.sendPrivateMsg(priMsg, str);
                }
            }
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = "关闭连接", code = master)
    public void closeConnection(PrivateMsg priMsg, MsgSender sender) {
        RemoteCommandUtil.closeConn(conn);
        sender.SENDER.sendPrivateMsg(priMsg, "已关闭连接。");
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = "开启连接", code = master)
    public void openConnection(PrivateMsg priMsg, MsgSender sender) {
        if (conn == null) {
            conn = RemoteCommandUtil.login(ip, userName, userPwd);
            sender.SENDER.sendPrivateMsg(priMsg, "已开启连接。");
        } else {
            sender.SENDER.sendPrivateMsg(priMsg, "已经打开连接，无需再开启。");
        }
    }

}
