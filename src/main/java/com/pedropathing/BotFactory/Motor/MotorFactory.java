package com.pedropathing.BotFactory.Motor;

import androidx.annotation.NonNull;

import com.pedropathing.BotFactory.Action;
import com.pedropathing.BotFactory.ConfigDirectionPair;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import static com.pedropathing.BotFactory.Action.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class  MotorFactory {
    private final ArrayList<DcMotorEx> ControlMotor= new ArrayList<>();
    private final int MotorNum;
    private final ArrayList<ConfigDirectionPair> Config;
    private final Map<Action, Integer> MotorAction;
    protected static HardwareMap hardwareMap;
    private static Action MotorState = Init;
    public MotorFactory(@NonNull MotorBuilder Builder){
        MotorNum=Builder.servoName.size();
        hardwareMap = Builder.hardwareMap;
        this.MotorAction = Collections.unmodifiableMap(new HashMap<>(Builder.actionMap));
        Config = new ArrayList<>(Builder.servoName);
        for(int i = 0;i < MotorNum;i++){
            ControlMotor.add(hardwareMap.get(DcMotorEx.class,Config.get(i).getConfig()));
            if(Config.get(i).isReverse()){
                ControlMotor.get(i).setDirection(DcMotorSimple.Direction.REVERSE);
            }
        }
    }
    public void Init(){
        for(int i = 0;i < MotorNum; i++){
            ControlMotor.get(i).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            ControlMotor.get(i).setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            ControlMotor.get(i).setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        MotorState = Init;
    }
    public void act(Action thisAction){
        if(!MotorAction.containsKey(thisAction)) {
            throw new NullPointerException("You used a fucking action that you didn't fucking told me!(｀Д´)");
        }
        for (int i = 0; i < MotorNum; i++) {
            ControlMotor.get(i).setTargetPosition(MotorAction.get(thisAction));
            ControlMotor.get(i).setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
        MotorState =thisAction;
    }
    public Action getState(){
        return MotorState;
    }
    public String getConfig(int i){
        if(i>=MotorNum){
            throw new IllegalArgumentException("Are you kidding me? I can't tell you a fucking servo name more than"+(MotorNum-1)+", but you asked me to tell you the "+i+"one!");
        }
        return Config.get(i).getConfig();
    }
    public boolean whichIsReversed(int i){
        if(i>=MotorNum){
            throw new IllegalArgumentException("Are you fucking kidding me? I can't tell you a fucking servo whether it is reversed more than"+(MotorNum-1)+", but you asked me to tell you the "+i+"one!");
        }
        return Config.get(i).isReverse();
    }
    public static class MotorBuilder {
        private final ArrayList<ConfigDirectionPair> servoName = new ArrayList<>();
        private final Map<Action, Integer> actionMap;
        private final HardwareMap hardwareMap;
        public MotorBuilder(String ConfigName1,int InitPosition,boolean isReverse,HardwareMap hardwareMap) {
            this.servoName.add(new ConfigDirectionPair(ConfigName1,isReverse));
            this.actionMap = new HashMap<>();
            this.actionMap.put(Init,InitPosition);
            this.hardwareMap = hardwareMap;
        }

        /**
         *给这个封装添加一个新的同步舵机
         *
         * @param newConfigName 添加舵机的名称
         * @param isReverse 是否反向
         * @return 当前Builder实例，实现链式调用
         */
        public MotorBuilder addServo(String newConfigName,boolean isReverse){
            servoName.add(new ConfigDirectionPair(newConfigName,isReverse));
            return this;
        }

        /**
         * 添加一个动作及其对应的Servo位置。
         *
         * @param actionType 动作的枚举类型
         * @param position   Servo的目标位置 (通常0.0到1.0之间)
         * @return 当前Builder实例，实现链式调用
         */
        public MotorBuilder addAction(Action actionType, int position) {
            actionMap.put(actionType, position);
            return this;
        }

        /**
         * 构建并返回一个 ServoFactory 实例。
         *
         * @return 构建好的 ServoFactory 对象
         */
        public MotorFactory build() {
            return new MotorFactory(this);
        }
    }
}
