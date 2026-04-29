package com.vserv.core.config;

import com.vserv.features.availability.service.SlotMaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once after the Spring context is fully started — the Spring equivalent
 * of the console app's {@code App.main()} slot bootstrap block:
 *
 * <pre>
 * AvailabilityManagementService mgmt = new AvailabilityManagementService();
 * mgmt.ensureTomorrowSlotsExist();
 * </pre>
 *
 * Any DB error is caught and logged so a transient connection hiccup at startup
 * never kills the whole application, matching the console's behaviour.
 */
@Component
public class ApplicationStartupRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ApplicationStartupRunner.class);

	private final SlotMaintenanceService slotMaintenanceService;

	public ApplicationStartupRunner(SlotMaintenanceService slotMaintenanceService) {
		this.slotMaintenanceService = slotMaintenanceService;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("[Startup] Running slot maintenance (cleanup past + seed today/tomorrow)...");
		try {
			slotMaintenanceService.runMaintenance();
			log.info("[Startup] Slot maintenance completed successfully");
		} catch (Exception e) {
			log.error("[Startup] Slot maintenance failed — app will continue but slots may be stale: {}",
					e.getMessage(), e);
		}
	}
}
