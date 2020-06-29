<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$taskName = $_REQUEST["taskName"];
$taskID = $_REQUEST["taskID"];

$sql = "UPDATE TASK SET Task_Name = ? WHERE Task_ID = ?";


$stmt = $link->prepare($sql);

if ($stmt){

	$stmt->bind_param("si",$taskName,$taskID);
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
