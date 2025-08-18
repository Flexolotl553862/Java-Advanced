package info.kgeorgiy.ja.morozov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {

    private static final Comparator<Student> STUDENT_COMPARATOR =
            Comparator.comparing(Student::firstName)
                    .thenComparing(Student::lastName)
                    .thenComparing(Student::id);

    private Stream<Student> getStream(Collection<Student> students) {
        return students.stream();
    }

    private <T> List<T> getFields(Collection<Student> students, Function<? super Student, T> f) {
        return getStream(students).map(f).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getFields(students, Student::firstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getFields(students, Student::lastName);
    }

    @Override
    public List<GroupName> getGroupNames(List<Student> students) {
        return getFields(students, Student::groupName);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getFields(students, student -> student.firstName() + " " + student.lastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return getStream(students).max(Comparator.naturalOrder()).map(Student::firstName).orElse("");
    }

    private Stream<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> cmp) {
        return sortStudentsBy(getStream(students), cmp);
    }

    private Stream<Student> sortStudentsBy(Stream<Student> students, Comparator<Student> cmp) {
        return students.sorted(cmp);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.naturalOrder()).toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, STUDENT_COMPARATOR).toList();
    }

    private <T> Stream<Student> findStudentsByField(Stream<Student> students,
                                                    Function<? super Student, T> getField,
                                                    T value) {
        return students.filter(student -> getField.apply(student).equals(value));
    }

    private <T> Stream<Student> findStudentsByField(Collection<Student> students,
                                                    Function<? super Student, T> getField,
                                                    T value) {

        return findStudentsByField(getStream(students), getField, value);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortStudentsBy(
                findStudentsByField(students, Student::firstName, name),
                STUDENT_COMPARATOR
        ).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsByField(students, Student::lastName, name).toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByField(sortStudentsBy(students, STUDENT_COMPARATOR), Student::groupName, group)
                .toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByField(students, Student::groupName, group)
                .collect(Collectors.toMap(
                        Student::lastName,
                        Student::firstName,
                        (name1, name2) -> name1.compareTo(name2) < 0 ? name1 : name2)
                );
    }

    private Stream<Map.Entry<GroupName, List<Student>>> getGroupLists(Collection<Student> students) {
        return getStream(students).collect(Collectors.groupingBy(Student::groupName)).entrySet().stream();
    }

    private Stream<Group> getSortedGroupLists(Collection<Student> students, Comparator<Student> cmp) {
        return getGroupLists(students)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new Group(e.getKey(), sortStudentsBy(e.getValue(), cmp).toList()));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroupLists(students, STUDENT_COMPARATOR).toList();
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroupLists(students, Comparator.naturalOrder()).toList();
    }

    private GroupName findMaxGroupBy(Collection<Student> students,
                                     Comparator<Map.Entry<GroupName, List<Student>>> cmp) {
        return getGroupLists(students).max(cmp).map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return findMaxGroupBy(students,
                Comparator.comparingInt((Map.Entry<GroupName, List<Student>> e) -> e.getValue().size())
                        .thenComparing(Map.Entry.comparingByKey()));
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return findMaxGroupBy(students,
                Comparator.comparingInt(
                                (Map.Entry<GroupName, List<Student>> e) -> getDistinctFirstNames(e.getValue()).size())
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())));
    }
}