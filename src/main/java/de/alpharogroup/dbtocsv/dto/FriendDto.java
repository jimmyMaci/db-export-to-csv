package de.alpharogroup.dbtocsv.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendDto {
 
    String firstname;
    String lastname;
    String city;

}