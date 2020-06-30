<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["userID"];

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

$sql = "SELECT (Password) FROM WITS WHERE User_ID = ?";
$stmt = $link->prepare($sql);

$stmt->bind_param("s",$userID);
$stmt->execute();

if ($result = $stmt->get_result()) {
		while ($row=$result->fetch_assoc()){
				$output[]=$row;
		}
}

$password = $output[0]['Password'];


$sql = "UPDATE LECTURER SET Lecturer_Password = ? WHERE Lecturer_ID = ?";


$stmt = $link->prepare($sql);

if ($stmt){

	$stmt->bind_param("ss",$password,$userID);
	$stmt->execute();

	if ($link->affected_rows > 0){
		$output = array();
		$output[] = "Successful";
	}else {
		$output = array();
		$output[] = "Error";
	}

} else {
	$output = array();
	$output[] = "Unsuccessful";
}

//standard connect

$stmt->close();
$link->close();
echo json_encode($output);
//end standard connect
?>
