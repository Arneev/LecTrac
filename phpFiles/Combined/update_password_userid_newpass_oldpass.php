<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["userID"];
$newPass = $_REQUEST["newPass"];
$oldPass = $_REQUEST["oldPass"];
$isLec = $_REQUEST["isLec"];

// length must be 7
if (strlen($userID) != 7){

	$output[] = "Error";
	echo json_encode($output);
	exit;
}

// Each character must be a Number
for ($i = 0; $i < 7; $i++){

	if (!is_numeric($userID[$i])){

		$output[] = "Error";
		echo json_encode($output);
		exit;
	}
}
//
$oldPassFromDb = "";

if ($isLec){
  $sql = "SELECT * FROM LECTURER WHERE Lecturer_ID = ?";
  $stmt = $link->prepare($sql);

  $stmt->bind_param("s",$userID);
  $stmt->execute();

  if ($result = $stmt->get_result()) {
  	while ($row=$result->fetch_assoc()){
  		$output[]=$row;
  	}
  }

  $oldPassFromDb = $output[0]['Lecturer_Password'];

}else {
  $sql = "SELECT * FROM STUDENT WHERE Student_ID = ?";
  $stmt = $link->prepare($sql);

  $stmt->bind_param("s",$userID);
  $stmt->execute();

  if ($result = $stmt->get_result()) {
  	while ($row=$result->fetch_assoc()){
  		$output[]=$row;
  	}
  }

  $oldPassFromDb = $output[0]['Student_Password'];
}

if ($oldPass != $oldPassFromDb){
  $output = array();
  $output[] = "Error";
  echo json_encode($output);
  exit;
}
$output = array();
//

$sql = "UPDATE LECTURER SET Lecturer_Password = ? WHERE Lecturer_ID = ?";


$stmt = $link->prepare($sql);

if ($stmt){

	$stmt->bind_param("ss",$newPass,$userID);
	$stmt->execute();

	if ($link->affected_rows > 0){
		$output[] = "Successful";
	}else {
		$output[] = "Error";
	}

} else {

	$output[] = "Unsuccessful";
}

//standard connect

$stmt->close();
$link->close();
echo json_encode($output);
//end standard connect
?>
