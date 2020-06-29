<?php
//standard connect
$username = "d2180393";
$password = "6R7g5UVB";
$database = "dLecTrac";
$link = mysqli_connect("127.0.0.1", $username, $password, $database);
$output=array();

$query = $_REQUEST["query"];

$result = $link->query($query);

if ($result){
    while($row = $result->fetch_assoc()){
      $output[] = $row;
    }
}
$link->close();
echo json_encode($output);

?>
