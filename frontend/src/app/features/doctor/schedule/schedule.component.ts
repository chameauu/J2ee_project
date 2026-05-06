import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorDashboardService } from '../../../core/services/doctor-dashboard.service';
import { Appointment } from '../../../core/models';

interface CalendarDay {
  date: number;
  dateKey: string;
  isEmpty: boolean;
  isToday: boolean;
  isSelected: boolean;
  appointments: Appointment[];
}

@Component({
  selector: 'app-doctor-schedule',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.scss'
})
export class DoctorScheduleComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DoctorDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly appointments = signal<Appointment[]>([]);
  protected readonly currentMonth = signal(new Date().getMonth());
  protected readonly currentYear = signal(new Date().getFullYear());
  protected readonly selectedDateKey = signal<string>('');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Computed values
  protected readonly calendarDays = computed(() => {
    return this.generateCalendarDays();
  });

  protected readonly selectedDayAppointments = computed(() => {
    const key = this.selectedDateKey();
    if (!key) return [];
    
    return this.appointments().filter(appt => {
      const apptDate = new Date(appt.appointmentDateTime);
      const apptKey = this.getDateKey(apptDate);
      return apptKey === key;
    }).sort((a, b) => 
      new Date(a.appointmentDateTime).getTime() - new Date(b.appointmentDateTime).getTime()
    );
  });

  protected readonly calendarTitle = computed(() => {
    const month = this.currentMonth();
    const year = this.currentYear();
    const date = new Date(year, month, 1);
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  });

  protected readonly selectedDateDisplay = computed(() => {
    const key = this.selectedDateKey();
    if (!key) return 'Select a date';
    
    const [year, month, day] = key.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', 
      month: 'long', 
      day: 'numeric',
      year: 'numeric'
    });
  });

  protected readonly doctorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Doctor';
  });

  protected readonly stats = computed(() => {
    const now = new Date();
    const allAppts = this.appointments();

    return {
      upcoming: allAppts.filter(a => 
        a.status === 'SCHEDULED' && new Date(a.appointmentDateTime) > now
      ).length,
      completed: allAppts.filter(a => a.status === 'COMPLETED').length,
      cancelled: allAppts.filter(a => a.status === 'CANCELLED').length
    };
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadAppointments();
  }

  /**
   * Load all appointments for the doctor
   */
  loadAppointments(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getDoctorAppointments(doctorId).subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        this.errorMessage.set('Failed to load appointments. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Generate calendar days for the current month
   */
  private generateCalendarDays(): CalendarDay[] {
    const month = this.currentMonth();
    const year = this.currentYear();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = firstDay.getDay();
    
    const days: CalendarDay[] = [];
    const today = new Date();
    const todayKey = this.getDateKey(today);
    const selectedKey = this.selectedDateKey();
    
    // Previous month days
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      days.push({
        date: prevMonthLastDay - i,
        dateKey: '',
        isEmpty: true,
        isToday: false,
        isSelected: false,
        appointments: []
      });
    }
    
    // Current month days
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const dateKey = this.getDateKey(date);
      const dayAppointments = this.getAppointmentsForDate(date);
      
      days.push({
        date: day,
        dateKey,
        isEmpty: false,
        isToday: dateKey === todayKey,
        isSelected: dateKey === selectedKey,
        appointments: dayAppointments
      });
    }
    
    // Next month days to fill the grid
    const remainingDays = 42 - days.length;
    for (let day = 1; day <= remainingDays; day++) {
      days.push({
        date: day,
        dateKey: '',
        isEmpty: true,
        isToday: false,
        isSelected: false,
        appointments: []
      });
    }
    
    return days;
  }

  /**
   * Get appointments for a specific date
   */
  private getAppointmentsForDate(date: Date): Appointment[] {
    const dateKey = this.getDateKey(date);
    return this.appointments().filter(appt => {
      const apptDate = new Date(appt.appointmentDateTime);
      return this.getDateKey(apptDate) === dateKey;
    });
  }

  /**
   * Get date key in YYYY-MM-DD format
   */
  private getDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Navigate to previous month
   */
  previousMonth(): void {
    const month = this.currentMonth();
    const year = this.currentYear();
    
    if (month === 0) {
      this.currentMonth.set(11);
      this.currentYear.set(year - 1);
    } else {
      this.currentMonth.set(month - 1);
    }
  }

  /**
   * Navigate to next month
   */
  nextMonth(): void {
    const month = this.currentMonth();
    const year = this.currentYear();
    
    if (month === 11) {
      this.currentMonth.set(0);
      this.currentYear.set(year + 1);
    } else {
      this.currentMonth.set(month + 1);
    }
  }

  /**
   * Go to today
   */
  goToToday(): void {
    const today = new Date();
    this.currentMonth.set(today.getMonth());
    this.currentYear.set(today.getFullYear());
    this.selectedDateKey.set(this.getDateKey(today));
  }

  /**
   * Select a day
   */
  selectDay(day: CalendarDay): void {
    if (day.isEmpty) return;
    this.selectedDateKey.set(day.dateKey);
  }

  /**
   * Get card class based on status
   */
  getCardClass(status: string): string {
    return status.toLowerCase();
  }

  /**
   * Get status badge class
   */
  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'SCHEDULED': 'badge-scheduled',
      'COMPLETED': 'badge-completed',
      'CANCELLED': 'badge-cancelled'
    };
    return map[status] || 'badge-secondary';
  }

  /**
   * Format appointment type
   */
  formatType(type: string): string {
    return type.split('_').map(word => 
      word.charAt(0) + word.slice(1).toLowerCase()
    ).join(' ');
  }

  /**
   * Format time
   */
  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Get current date formatted
   */
  getCurrentDate(): string {
    const now = new Date();
    return now.toLocaleDateString('en-GB', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  /**
   * Get initials from name
   */
  getInitials(name: string): string {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
