package floris0106.yolt.util;

public interface ServerPlayerExtension
{
	int yolt$getLives();
	void yolt$setLives(int lives);
    float yolt$getTotalHealth();
    void yolt$setTotalHealth(float total);
    void yolt$updateLives();
	Role yolt$getRole();
	void yolt$setRole(Role role);
	void yolt$revealTargets();
}