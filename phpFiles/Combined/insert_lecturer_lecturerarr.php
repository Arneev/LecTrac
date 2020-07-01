<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["userID"];
$firstName = $_REQUEST["firstName"];
$lastName = $_REQUEST["lastName"];
$email = $_REQUEST["email"];
$nick = $_REQUEST["nick"];
$password = $_REQUEST["password"];


$sql = "INSERT INTO LECTURER (Lecturer_ID,Lecturer_FName,Lecturer_LName,Lecturer_Email,Lecturer_Reference,Lecturer_Password) VALUES(?,?,?,?,?,?)";
$stmt = $link->prepare($sql);
$stmt->bind_param("ssssss",$userID,$firstName,$lastName,$email,$nick,$password);

//standard connect
if ($stmt->execute()){
	$output = array();
	$output[] = "Successful";
	echo json_encode($output);

	//TAKE FROM WITS DB AND INTO LECTRAC DB

	$syncSql = "INSERT INTO REGISTERED SELECT * FROM WITS_REGISTERED WHERE WITS_REGISTERED.Lecturer_ID = ?";
	$syncStmt = $link->prepare($syncSql);
	$syncStmt->bind_param("s",$userID);
	$syncStmt->execute();


	//TAKE FROM WITS DB AND INTO LECTRAC DB - END

}else{
	$output = array();
	$output[] = "Unsuccessful";
	echo json_encode($output);
}
$stmt->close();
$link->close();
exit;
//end standard connect
?>
