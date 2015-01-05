package ca.digitalcave.scheduler.data;

import org.apache.ibatis.session.ResultHandler;

public interface RoomMapper {
	void select(ResultHandler handler);
	void insert(Id<Integer> id, String name);
	void update(int id, String name);
	void delete(int id);
}
