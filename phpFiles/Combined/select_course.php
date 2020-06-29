<?php
//standard connect
$username = "d2180393";
$dbPass = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $dbPass, $database);
$output=array();
//end standard connect

$sql = "SELECT * FROM COURSE";

$result = $link->query($sql);

//standard connect
if ($result) {
	while ($row=$result->fetch_assoc()){
		$output[]=$row;
	}
}

$link->close();
echo json_encode($output);
//end standard connect
?>
