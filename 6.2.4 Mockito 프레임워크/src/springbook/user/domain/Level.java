package springbook.user.domain;

//사용자 레벨용 이늄
//이렇게 만들어진 Level 이늄은 내부에는 DB에 저장할 int타입의 값을 갖고 있지만,
// 겉으로는 Level 타입의 오브젝트 이기 때문에 안전하게 사용할 수 있다.
public enum Level {
	GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);

	private final int value;
	private final Level next;

	public Level getNext() {
		return next;
	}

	Level(int value, Level next) {
		// DB에 저장할 값을 넣어줄 생성자를 만들어 둔다.
		this.value = value;
		this.next = next;
	}

	public int initValue() {
		return value;
	}
	public Level nextLevel(){
		return this.next;
	}
	
	public static Level valueOf(int value) {
		switch (value) {
		case 1:
			return BASIC;
		case 2:
			return SILVER;
		case 3:
			return GOLD;

		default:
			throw new AssertionError("Unknown value:" + value);

		}
	}
}
