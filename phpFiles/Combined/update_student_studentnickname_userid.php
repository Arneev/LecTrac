<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$usernickname = $_REQUEST["nickname"];
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

$sql = "UPDATE STUDENT SET Student_Nickname = ? WHERE Student_ID = ?";


$stmt = $link->prepare($sql);


if ($stmt){

	$stmt->bind_param("ss",$usernickname,$userID);
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
