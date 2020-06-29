<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$userID = $_REQUEST["studentID"];
$sql = "SELECT * FROM STUDENT WHERE Student_ID = ?";

//start of userID check
$size = strlen($userID);
if ($size != 7){
    $output = array();
    $output[] = "Error";
    echo json_encode($output);
    exit;
}

try {
    $iUserID = (int)$userID;
} catch (Exception $e) {
  $output = array();
  $output[] = "Error";
  echo json_encode($output);
  exit;
}

//end of userID check


$stmt = $link->prepare($sql);
$stmt->bind_param("s",$userID);
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
