<?php
//standard connect
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $password, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["studentId"];
$firstName = $_REQUEST["firstName"];
$lastName = $_REQUEST["lastName"];
$email = $_REQUEST["email"];
$nick = $_REQUEST["nickname"];
$password = $_REQUEST["password"];

$sql = "INSERT INTO LECTURER VALUES(?,?,?,?,?,?)";

$stmt = $link->prepare($sql);
$stmt->bind_param("ssssss",$userID,$firstName,$lastName,$email,$nick,$password);

if ($result = $stmt->get_result()) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

$stmt->close();
$link->close();
echo json_encode($output);
?>
