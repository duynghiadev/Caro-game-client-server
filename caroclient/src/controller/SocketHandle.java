/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import model.User;

/**
 *
 * @author Admin
 */
public class SocketHandle implements Runnable {
    private BufferedWriter os;
    private BufferedReader is;
    private Socket socketOfClient;
    private int ID_Server;
    public List<User> getListUser(String[] message){
        List<User> friend = new ArrayList<>();
        for(int i=1; i<message.length; i=i+4){
            friend.add(new User(Integer.parseInt(message[i]),
                    message[i+1],
                    message[i+2].equals("1"),
                    message[i+3].equals("1")));
        }
        return friend;
    }
    public List<User> getListRank(String[] message){
        List<User> friend = new ArrayList<>();
        for(int i=1; i<message.length; i=i+9){
            friend.add(new User(Integer.parseInt(message[i]),
                message[i+1],
                message[i+2],
                message[i+3],
                message[i+4],
                Integer.parseInt(message[i+5]),
                Integer.parseInt(message[i+6]),
                Integer.parseInt(message[i+7]),
                Integer.parseInt(message[i+8])));
        }
        return friend;
    }
    public User getUserFromString(int start, String[] message){
        return new User(Integer.parseInt(message[start]),
                message[start+1],
                message[start+2],
                message[start+3],
                message[start+4],
                Integer.parseInt(message[start+5]),
                Integer.parseInt(message[start+6]),
                Integer.parseInt(message[start+7]),
                Integer.parseInt(message[start+8]));
    }
    @Override
    public void run() {

        try {
            // G???i y??u c???u k???t n???i t???i Server ??ang l???ng nghe
            socketOfClient = new Socket("127.0.0.1", 7777);
            System.out.println("K???t n???i th??nh c??ng!");
            // T???o lu???ng ?????u ra t???i client (G???i d??? li???u t???i server)
            os = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
            // Lu???ng ?????u v??o t???i Client (Nh???n d??? li???u t??? server).
            is = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
            String message;
            while (true) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                String[] messageSplit = message.split(",");
                if(messageSplit[0].equals("server-send-id")){
                    ID_Server = Integer.parseInt(messageSplit[1]);
                }
                //????ng nh???p th??nh c??ng
                if(messageSplit[0].equals("login-success")){
                    System.out.println("????ng nh???p th??nh c??ng");
                    Client.closeAllViews();
                    User user= getUserFromString(1,messageSplit);
                    Client.user = user;
                    Client.openView(Client.View.HOMEPAGE);
                }
                //Th??ng tin t??i kho???n sai
                if(messageSplit[0].equals("wrong-user")){
                    System.out.println("Th??ng tin sai");
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.openView(Client.View.LOGIN,messageSplit[1],messageSplit[2]);
                    Client.loginFrm.showError("T??i kho???n ho???c m???t kh???u kh??ng ch??nh x??c");
                }
                //T??i kho???n ???? ????ng nh???p ??? n??i kh??c
                if(messageSplit[0].equals("dupplicate-login")){
                    System.out.println("???? ????ng nh???p");
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.openView(Client.View.LOGIN,messageSplit[1],messageSplit[2]);
                    Client.loginFrm.showError("T??i kho???n ???? ????ng nh???p ??? n??i kh??c");
                }
                //T??i kho???n ???? b??? banned
                if(messageSplit[0].equals("banned-user")){
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.openView(Client.View.LOGIN,messageSplit[1],messageSplit[2]);
                    Client.loginFrm.showError("T??i kho???n ???? b??? ban");
                }
                //X??? l?? register tr??ng t??n
                if(messageSplit[0].equals("duplicate-username")){
                    Client.closeAllViews();
                    Client.openView(Client.View.REGISTER);
                    JOptionPane.showMessageDialog(Client.registerFrm, "T??n t??i kho???n ???? ???????c ng?????i kh??c s??? d???ng");
                }
                //X??? l?? nh???n th??ng tin, chat t??? to??n server
                if(messageSplit[0].equals("chat-server")){
                    if(Client.homePageFrm!=null){
                        Client.homePageFrm.addMessage(messageSplit[1]);
                    }
                }
                //X??? l?? hi???n th??? th??ng tin ?????i th??? l?? b???n b??/kh??ng
                if(messageSplit[0].equals("check-friend-response")){
                    if(Client.competitorInfoFrm!=null){
                        Client.competitorInfoFrm.checkFriend((messageSplit[1].equals("1")));
                    }
                }
                //X??? l?? k???t qu??? t??m ph??ng t??? server
                if(messageSplit[0].equals("room-fully")){
                    Client.closeAllViews();
                    Client.openView(Client.View.HOMEPAGE);
                    JOptionPane.showMessageDialog(Client.homePageFrm, "Ph??ng ch??i ???? ????? 2 ng?????i ch??i");
                }
                // X??? l?? kh??ng t??m th???y ph??ng trong ch???c n??ng v??o ph??ng
                if(messageSplit[0].equals("room-not-found")){
                    Client.closeAllViews();
                    Client.openView(Client.View.HOMEPAGE);
                    JOptionPane.showMessageDialog(Client.homePageFrm, "Kh??ng t??m th???y ph??ng");
                }
                // X??? l?? ph??ng c?? m???t kh???u sai
                if(messageSplit[0].equals("room-wrong-password")){
                    Client.closeAllViews();
                    Client.openView(Client.View.HOMEPAGE);
                    JOptionPane.showMessageDialog(Client.homePageFrm, "M???t kh???u ph??ng sai");
                }
                //X??? l?? xem rank
                if(messageSplit[0].equals("return-get-rank-charts")){
                    if(Client.rankFrm!=null){
                        Client.rankFrm.setDataToTable(getListRank(messageSplit));
                    }
                }
                //X??? l?? l???y danh s??ch ph??ng
                if(messageSplit[0].equals("room-list")){
                    Vector<String> rooms = new Vector<>();
                    Vector<String> passwords = new Vector<>();
                    for(int i=1; i<messageSplit.length; i=i+2){
                        rooms.add("Ph??ng "+messageSplit[i]);
                        passwords.add(messageSplit[i+1]);
                    }
                    Client.roomListFrm.updateRoomList(rooms,passwords);
                }
                if(messageSplit[0].equals("return-friend-list")){
                    if(Client.friendListFrm!=null){
                        Client.friendListFrm.updateFriendList(getListUser(messageSplit));
                    }
                }
                if(messageSplit[0].equals("go-to-room")){
                    System.out.println("V??o ph??ng");
                    int roomID = Integer.parseInt(messageSplit[1]);
                    String competitorIP = messageSplit[2];
                    int isStart = Integer.parseInt(messageSplit[3]);
                    
                    User competitor = getUserFromString(4, messageSplit);
                    if(Client.findRoomFrm!=null){
                        Client.findRoomFrm.showFindedRoom();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            JOptionPane.showMessageDialog(Client.findRoomFrm, "L???i khi sleep thread");
                        }
                    } else if(Client.waitingRoomFrm!=null){
                        Client.waitingRoomFrm.showFindedCompetitor();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            JOptionPane.showMessageDialog(Client.waitingRoomFrm, "L???i khi sleep thread");
                        }
                    }
                    Client.closeAllViews();
                    System.out.println("???? v??o ph??ng: "+roomID);
                    //X??? l?? v??o ph??ng
                    Client.openView(Client.View.GAMECLIENT
                            , competitor
                            , roomID
                            ,isStart
                            ,competitorIP);
                    Client.gameClientFrm.newgame();
                }
                //T???o ph??ng v?? server tr??? v??? t??n ph??ng
                if(messageSplit[0].equals("your-created-room")){
                    Client.closeAllViews();
                    Client.openView(Client.View.WAITINGROOM);
                    Client.waitingRoomFrm.setRoomName(messageSplit[1]);
                    if(messageSplit.length==3)
                        Client.waitingRoomFrm.setRoomPassword("M???t kh???u ph??ng: "+messageSplit[2]);
                }
                //X??? l?? y??u c???u k???t b???n t???i
                if(messageSplit[0].equals("make-friend-request")){
                    int ID = Integer.parseInt(messageSplit[1]);
                    String nickname = messageSplit[2];
                    Client.openView(Client.View.FRIENDREQUEST, ID, nickname);
                }
                //X??? l?? khi nh???n ???????c y??u c???u th??ch ?????u
                if(messageSplit[0].equals("duel-notice")){
                    int res = JOptionPane.showConfirmDialog(Client.getVisibleJFrame(), "B???n nh???n ???????c l???i th??ch ?????u c???a "+messageSplit[2]+" (ID="+messageSplit[1]+")", "X??c nh???n th??ch ?????u", JOptionPane.YES_NO_OPTION);
                    if(res == JOptionPane.YES_OPTION){
                        Client.socketHandle.write("agree-duel,"+messageSplit[1]);
                    }
                    else{
                        Client.socketHandle.write("disagree-duel,"+messageSplit[1]);
                    }
                }
                //X??? l?? kh??ng ?????ng ?? th??ch ?????u
                if(messageSplit[0].equals("disagree-duel")){
                    Client.closeAllViews();
                    Client.openView(Client.View.HOMEPAGE);
                    JOptionPane.showMessageDialog(Client.homePageFrm, "?????i th??? kh??ng ?????ng ?? th??ch ?????u");
                }
                //X??? l?? ????nh m???t n?????c trong v??n ch??i
                if(messageSplit[0].equals("caro")){
                    Client.gameClientFrm.addCompetitorMove(messageSplit[1], messageSplit[2]);
                }
                if(messageSplit[0].equals("chat")){
                    Client.gameClientFrm.addMessage(messageSplit[1]);
                }
                if(messageSplit[0].equals("draw-request")){
                    Client.gameClientFrm.showDrawRequest();
                }
                
                if(messageSplit[0].equals("draw-refuse")){
                    if(Client.gameNoticeFrm!=null) Client.closeView(Client.View.GAMENOTICE);
                    Client.gameClientFrm.displayDrawRefuse();
                }
                
                if(messageSplit[0].equals("new-game")){
                    System.out.println("New game");
                    Thread.sleep(4000);
                    Client.gameClientFrm.updateNumberOfGame();
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.gameClientFrm.newgame();
                }
                if(messageSplit[0].equals("draw-game")){
                    System.out.println("Draw game");
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.openView(Client.View.GAMENOTICE, "V??n ch??i h??a", "V??n ch??i m???i dang ???????c thi???t l???p");
                    Client.gameClientFrm.displayDrawGame();
                    Thread.sleep(4000);
                    Client.gameClientFrm.updateNumberOfGame();
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.gameClientFrm.newgame();
                }
                if(messageSplit[0].equals("competitor-time-out")){
                    Client.gameClientFrm.increaseWinMatchToUser();
                    Client.openView(Client.View.GAMENOTICE,"B???n ???? th???ng do ?????i th??? qu?? th???i gian","??ang thi???t laapju v??n ch??i m???i");
                    Thread.sleep(4000);
                    Client.closeView(Client.View.GAMENOTICE);
                    Client.gameClientFrm.updateNumberOfGame();
                    Client.gameClientFrm.newgame();
                }
                if(messageSplit[0].equals("voice-message")){
                    switch (messageSplit[1]) {
                        case "close-mic":
                            Client.gameClientFrm.addVoiceMessage("???? t???t mic");
                            break;
                        case "open-mic":
                            Client.gameClientFrm.addVoiceMessage("???? b???t mic");
                            break;
                        case "close-speaker":
                            Client.gameClientFrm.addVoiceMessage("???? t???t ??m thanh cu???c tr?? chuy???n");
                            break;
                        case "open-speaker":
                            Client.gameClientFrm.addVoiceMessage("???? b???t ??m thanh cu???c tr?? chuy???n");
                            break;
                    }
                }
                if(messageSplit[0].equals("left-room")){
                    Client.gameClientFrm.stopTimer();
                    Client.closeAllViews();
                    Client.openView(Client.View.GAMENOTICE,"?????i th??? ???? tho??t kh???i ph??ng","??ang tr??? v??? trang ch???");
                    Thread.sleep(3000);       
                    Client.closeAllViews();
                    Client.openView(Client.View.HOMEPAGE);
                }
                //X??? l?? b??? banned
                if(messageSplit[0].equals("banned-notice")){
                    Client.socketHandle.write("offline,"+Client.user.getID());
                    Client.closeAllViews();
                    Client.openView(Client.View.LOGIN);
                    JOptionPane.showMessageDialog(Client.loginFrm, messageSplit[1], "B???n ???? b??? BAN", JOptionPane.WARNING_MESSAGE);
                }
                //X??? l?? c???nh c??o
                if(messageSplit[0].equals("warning-notice")){
                    JOptionPane.showMessageDialog(null, messageSplit[1] , "B???n nh???n ???????c m???t c???nh c??o", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void write(String message) throws IOException{
        os.write(message);
        os.newLine();
        os.flush();
    }

    public Socket getSocketOfClient() {
        return socketOfClient;
    }

}
