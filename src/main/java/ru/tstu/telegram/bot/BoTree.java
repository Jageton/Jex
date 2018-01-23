package ru.tstu.telegram.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

interface  IResponseDialog{
    boolean GetResponseDialog(SendMessage response, String text, Integer userId, int number);
}

class BotTree{
    private HashMap<Integer,Node> Nodes;
    private HashMap<Long,NODE> CurrentStates;
    public BotTree(){
        Nodes = new HashMap<>();
    }
    private  boolean isDialog;
    private Dialog DialogSession;

    public int Init(Node node){
        int i = Nodes.size();
        Nodes.put(Nodes.size(), node);
        CurrentStates = new HashMap<>();
        return i;
    }

    public boolean ChatIsNew(Long chatId)
    {
        return CurrentStates.containsKey(chatId);
    }

    public int AddNode(int index,Node node) {
        int i = Nodes.size();
        if (index <= -1) {
            index = 0;
        }
        String name = node.GetName();
        Node buf = Nodes.get(index);
        ParentNode pn = new ParentNode(NODE.Status.PorterNode,buf);
        pn.setName("Back");
        pn.setKeyboardButton(new KeyboardButton().setText("Back"));
        node.AddChildNode(pn, "Back");
        buf.AddChildNode(node, name);
        Nodes.replace(index, buf);
        Nodes.put(i, node);
        return i;
    }

    public void InitNewChat(long chatId){
        CurrentStates.put(chatId, Nodes.get(0));
    }

    public SendMessage GetResponse(SendMessage response, String text, long chatId, Integer userId){
        if(isDialog){
            isDialog = DialogSession.EnterDialog(response, text, userId);
            return response;
        }
        NODE buf = CurrentStates.get(chatId).getNextNode(text);
        if(buf != null) {
            if(buf.getNodeStatus() == NODE.Status.PorterNode) {
                CurrentStates.put(chatId, buf);
            }else if(buf.getNodeStatus() == Node.Status.DialogNode){
                DialogSession = new Dialog((Node)buf);
                isDialog = true;
                DialogSession.EnterDialog(response, text, userId);
                return response;
            }
            response.setText(buf.getName());
            return buf.Get(response, userId);
        }
        response.setText("Start");
        return CurrentStates.get(chatId).Get(response,userId);
    }
}
@XmlRootElement
abstract  class NODE implements  IResponse{
    enum Status { PorterNode, FinalNode, DialogNode }
    protected   Status NodeStatus;
    protected   KeyboardButton keyboardButton;
    protected   String Name;
    protected   IResponse Iresponse;

    public NODE(Status status){
        NodeStatus = status;
    }

    public NODE(){}

    public IResponse getIResponse(){
        return Iresponse;
    }

    public void setIResponse(IResponse iresponse)
    {
        Iresponse = iresponse;
    }

    public void setName(String name)
    {
        this.Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setKeyboardButton(KeyboardButton k){
        keyboardButton = k;
    }

    public KeyboardButton getKeyboardButton() {
        return keyboardButton;
    }

    public Status getNodeStatus() {
        return NodeStatus;
    }
    public void setNodeStatus(Status status){
        NodeStatus = status;
    }

    public abstract SendMessage Get(SendMessage reponse, Integer userId);
    public abstract NODE getNextNode(String name);
    public abstract NODE getNode();

}

class ParentNode extends NODE{

    Node parentNode;
    public ParentNode(Status status, Node parentNode)
    {
        super(status);
        this.parentNode = parentNode;
    }

    @Override
    public SendMessage Get(SendMessage reponse, Integer userId) {
        return null;
    }

    @Override
    public NODE getNextNode(String name) {
        return parentNode;
    }

    @Override
    public NODE getNode(){
        return parentNode;
    }

    @Override
    public SendMessage GetResponse(SendMessage response, Integer userId) {
        return null;
    }
}

interface IResponse {
    SendMessage GetResponse(SendMessage response, Integer userId);
}

class Node extends NODE implements  IResponse, IResponseDialog{
    private HashMap<String,Integer> Indexes;
    private List<NODE> ChildNodes;
    private IResponseDialog IresponseDialog;

    public String GetName(){
        return Name;
    }

    public void SetName(String name)
    {
        this.Name = name;
    }

    public Node(NODE.Status status){
        super(status);
        ChildNodes = new ArrayList<>();
        Indexes = new HashMap<>();
    }

    public void setIresponseDialog(IResponseDialog ird)
    {
        IresponseDialog = ird;
    }

    public IResponseDialog getIresponseDialog() {
        return IresponseDialog;
    }

    public void SetIResponse(IResponse Iresponse){
        this.Iresponse = Iresponse;
    }

    public boolean IsPorterNode(){
        return NodeStatus == Status.PorterNode;
    }

    @Override
    public SendMessage GetResponse(SendMessage response,Integer userId) {
        return Iresponse.GetResponse(response, userId);
    }

    @Override
    public SendMessage Get(SendMessage response, Integer userId){
        switch (NodeStatus){
            case FinalNode:
                GetResponse(response, userId);
                break;
            case PorterNode:
                GetButtons(response);
                break;
        }
        return response;
    }

    public void AddChildNode(NODE node, String name){
        Indexes.put(name, ChildNodes.size());
        ChildNodes.add(node);
    }

    public SendMessage GetButtons(SendMessage response){
        KeyboardRow row = new KeyboardRow();
        for(NODE node : ChildNodes){
            row.add(node.getKeyboardButton());
        }
        List<KeyboardRow> rows = new ArrayList<KeyboardRow>();
        rows.add(row);
        response.setReplyMarkup(new ReplyKeyboardMarkup()
                .setResizeKeyboard(true)
                .setKeyboard(rows)
        );
        return response;
    }

    @Override
    public NODE getNextNode(String name){
        try{
            int i = Indexes.get(name);
            return ChildNodes.get(i).getNode();
        }catch(Exception e)
        {
            return null;
        }
    }

    @Override
    public NODE getNode(){
        return this;
    }

    @Override
    public boolean GetResponseDialog(SendMessage response, String text, Integer userId, int number) {
       return IresponseDialog.GetResponseDialog(response, text, userId, number);
    }
}

