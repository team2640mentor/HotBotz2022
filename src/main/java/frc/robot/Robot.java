/*
  2022 everybot code
  written by carson graf 
  don't email me, @ me on discord
*/

/*
  This is catastrophically poorly written code for the sake of being easy to follow
  If you know what the word "refactor" means, you should refactor this code
*/

package frc.robot;


import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
  
  //Definitions for the hardware. Change this if you change what stuff you have plugged in
  CANSparkMax driveLeftA = new CANSparkMax(1, MotorType.kBrushed);
  CANSparkMax driveLeftB = new CANSparkMax(2, MotorType.kBrushed);
  CANSparkMax driveRightA = new CANSparkMax(3, MotorType.kBrushed);
  CANSparkMax driveRightB = new CANSparkMax(4, MotorType.kBrushed);
  CANSparkMax arm = new CANSparkMax(5, MotorType.kBrushless);
  CANSparkMax intakeA = new CANSparkMax(6, MotorType.kBrushed);
  CANSparkMax intakeB = new CANSparkMax(7, MotorType.kBrushed);
  Joystick driverController = new Joystick(0);
  Joystick armController = new Joystick(1); 
  

  //*Constants for controlling the arm. consider tuning these for your particular robot
  final double armHoldUp = .06; 
  final double armHoldDown = .02; 
  final double armTravel = .35; 
  final double armTimeUp = 1.62;   
  final double armTimeDown = 1.575; 


  //Varibles needed for the code
  boolean armUp = true; //Arm initialized to up because that's how it would start a match
  boolean burstMode = false;
  double lastBurstTime = 0;

  double autoStart = 0;
  boolean goForAuto = false;

  double counterclock = 1;   //intake
  double clock = -1;        //outake


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    //Configure motors to turn correct direction. You may have to invert some of your motors, orginally Driveleftb(true),Driverightb(false)
    //added intakeB.follow(intakeA); to synchronize intake motors
    driveLeftA.setInverted(true);   
    driveLeftA.burnFlash();
    driveLeftB.setInverted(true);
    driveLeftB.burnFlash();
    driveRightA.setInverted(false);
    driveRightA.burnFlash();
    driveRightB.setInverted(false);
    driveRightB.burnFlash();
    //intakeB.follow(intakeA);    
    arm.setInverted(false);
    arm.setIdleMode(IdleMode.kBrake);
    arm.burnFlash();

    //add a thing on the dashboard to turn off auto if needed
    SmartDashboard.putBoolean("Go For Auto", false);
    goForAuto = SmartDashboard.getBoolean("Go For Auto", false);
  }

  @Override
  public void autonomousInit() {
    //get a time for auton start to do events based on time later
    autoStart = Timer.getFPGATimestamp();
    //check dashboard icon to ensure good to do auto
    goForAuto = true;//SmartDashboard.getBoolean("Go For Auto", false);
  }

  //This function is called periodically during autonomous. 
  @Override
  public void autonomousPeriodic() {
    //arm control code. same as in teleop
    if(armUp){
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeUp){
        arm.set(armTravel);
      }
      else{
        arm.set(armHoldUp);
      }
    }
    else{
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeDown){
        arm.set(-armTravel);
      }
      else{
        arm.set(-armHoldUp);
      }
    }
    
    //get time since start of auto
    double autoTimeElapsed = Timer.getFPGATimestamp() - autoStart;
    if(goForAuto){
      if(autoTimeElapsed < 1.25){
        intakeA.set(0);
        intakeB.set(0);
        driveLeftA.set(0.2);
        driveLeftB.set(0.2);
        driveRightA.set(0.2);
        driveRightB.set(0.2);
      //series of timed events making up the flow of auto
      }if(autoTimeElapsed < 1.5){
        //spit out the ball for three seconds
        intakeA.set(clock);  //-1 
        intakeB.set(counterclock);  //-1
      }else if(autoTimeElapsed < 8){
        //stop spitting out the ball and drive backwards *slowly* for three seconds
        intakeA.set(0);
        intakeB.set(0);
        driveLeftA.set(-0.2);
        driveLeftB.set(-0.2);
        driveRightA.set(-0.2);
        driveRightB.set(-0.2);
      }else{
        //do nothing for the rest of auto
        intakeA.set(0);
        intakeB.set(0);
        driveLeftA.set(0);
        driveLeftB.set(0);
        driveRightA.set(0);
        driveRightB.set(0);
      }}
    }
  

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    //Set up arcade steer
  
    double forward= -driverController.getRawAxis(1);
    double turn = -driverController.getRawAxis(4);
    double driveLeftPower = forward - turn;
    double driveRightPower = forward + turn;

    driveLeftA.set(driveLeftPower);
    driveLeftB.set(driveLeftPower);
    driveRightA.set(driveRightPower);
    driveRightB.set(driveRightPower);

    //Intake controls   //6=intake   //4=shoot
    if(armController.getRawButton(6)){   
      intakeA.set(clock);   //-1,1
      intakeB.set(counterclock); 
    }
    else{ if(armController.getRawButton(4)){   
      intakeA.set(counterclock);  //-1 //1 orginally   intake 2 with 1,-1 intakea/b goes for shooting
      intakeB.set(clock);  //orginallyec
    } 
    else{
      intakeA.set(0);
      intakeB.set(0); 
    }

    //Arm Controls
    if(armUp){
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeUp){
        arm.set(armTravel);
      }
      else{
        arm.set(armHoldUp);
      }
    }
    else{
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeDown){
        arm.set(-armTravel);
      }
      else{
        arm.set(-armHoldDown);
      }
    }
  
    if(armController.getRawButtonPressed(5) && !armUp){ 
      lastBurstTime = Timer.getFPGATimestamp();
      armUp = true;
    }
    else if(armController.getRawButtonPressed(3) && armUp){
      lastBurstTime = Timer.getFPGATimestamp();
      armUp = false;}}
    }  

  

  @Override
  public void disabledInit() {
    //On disable turn off everything
    //done to solve issue with motors "remembering" previous setpoints after reenable
    driveLeftA.set(0);
    driveLeftB.set(0);
    driveRightA.set(0);
    driveRightB.set(0);
    arm.set(0);
    intakeA.set(0);
    intakeB.set(0);
  }
    
}