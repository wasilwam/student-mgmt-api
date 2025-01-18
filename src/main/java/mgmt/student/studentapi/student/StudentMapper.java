package mgmt.student.studentapi.student;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentOR toEntity(Student s);
    Student toEntity(StudentOR s);
}
