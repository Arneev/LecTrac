<?php
//standard connect
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $password, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["userid"];
$sql = "SELECT * FROM STUDENT WHERE Student_ID = ";
$sql .= $userID;
$sql .= " UNION SELECT * FROM LECTURER WHERE Lecturer_ID = ";
$sql .= $userID;


$stmt = $link->prepare($sql);
$stmt->execute();

if ($result = $stmt->get_result()) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

$stmt->close();
$link->close();
echo json_encode($output);
?>
