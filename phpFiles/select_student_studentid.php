<?php
//standard connect
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $password, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["studentId"];
$sql = "SELECT * FROM STUDENT WHERE Student_ID = ?";

$stmt = $link->prepare($sql);
$stmt = $link->bind_param
$stmt->bind_param("s",$userID);

if ($result = $stmt->get_result()) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

$stmt->close();
$link->close();
echo json_encode($output);
?>
