package ch.sourcepond.maven.release.config;

import java.io.File;
import java.util.List;

import org.apache.maven.settings.Settings;

public interface Configuration {

	Long getBuildNumber();

	List<String> getModulesToRelease();

	List<String> getModulesToForceRelease();

	boolean isDisableSshAgent();

	boolean isDebugEnabled();

	boolean isStacktraceEnabled();

	Settings getSettings();

	String getServerId();

	String getKnownHosts();

	String getPrivateKey();

	String getPassphrase();

	List<String> getGoals();

	List<String> getReleaseProfiles();

	boolean isIncrementSnapshotVersionAfterRelease();

	boolean isSkipTests();

	File getUserSettings();

	File getGlobalSettings();

	File getLocalMavenRepo();

}