import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PatientDashboardService } from '../../../core/services/patient-dashboard.service';
import { Appointment } from '../../../core/models';

interface CalendarDay {
  date: number;
  dateKey: string;
  isToday: boolean;
  isSelected: boolean;
  isEmpty: boolean;
  appointments: Appointment[];
}

@Component({
  selector: 'app-appointments',
  imports: [CommonModule, RouterLink],
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.scss'
})
export class AppointmentsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(PatientDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly appointments = signal<Appointment[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');
  
  // Calendar state
  protected readonly viewYear = signal(new Date().getFullYear());
  protected readonly viewMonth = signal(new Date().getMonth());
  protected readonly selectedDateKey = signal<string | null>(null);
  protected readonly today = new Date();

  // Computed values
  protected readonly calendarTitle = computed(() => {
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 
                    'July', 'August', 'September', 'October', 'November', 'December'];
    return `${months[this.viewMonth()]} ${this.viewYear()}`;
  });

  protected readonly calendarDays = computed(() => {
    return this.generateCalendarDays();
  });

  protected readonly selectedDayAppointments = computed(() => {
    const key = this.selectedDateKey();
    if (!key) return [];
    return this.appointments().filter(a => this.getDateKey(new Date(a.appointmentDateTime)) === key);
  });

  protected readonly selectedDateDisplay = computed(() => {
    const key = this.selectedDateKey();
    if (!key) return 'Select a day';
    
    const [year, month, day] = key.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 
                    'July', 'August', 'September', 'October', 'November', 'December'];
    
    return `${days[date.getDay()]}, ${day} ${months[date.getMonth()]} ${year}`;
  });

  protected readonly stats = computed(() => {
    const appts = this.appointments();
    return {
      upcoming: appts.filter(a => a.status === 'SCHEDULED').length,
      completed: appts.filter(a => a.status === 'COMPLETED').length,
      cancelled: appts.filter(a => a.status === 'CANCELLED').length
    };
  });

  protected readonly patientName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Patient';
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadAppointments();
  }

  /**
   * Load appointments from backend
   */
  loadAppointments(): void {
    const patientId = this.authService.getCurrentUserId();
    if (!patientId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getAppointments(patientId).subscribe({
      next: (data) => {
        this.appointments.set(data);
        this.isLoading.set(false);
        
        // Auto-select today if it has appointments
        const todayKey = this.getDateKey(this.today);
        const todayAppts = data.filter(a => this.getDateKey(new Date(a.appointmentDateTime)) === todayKey);
        if (todayAppts.length > 0) {
          this.selectedDateKey.set(todayKey);
        }
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        this.errorMessage.set('Failed to load appointments. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Generate calendar days for current view
   */
  private generateCalendarDays(): CalendarDay[] {
    const year = this.viewYear();
    const month = this.viewMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const days: CalendarDay[] = [];

    // Empty cells before first day
    for (let i = 0; i < firstDay; i++) {
      days.push({
        date: 0,
        dateKey: '',
        isToday: false,
        isSelected: false,
        isEmpty: true,
        appointments: []
      });
    }

    // Actual days
    for (let d = 1; d <= daysInMonth; d++) {
      const dateKey = this.getDateKey(new Date(year, month, d));
      const dayAppointments = this.appointments().filter(a => 
        this.getDateKey(new Date(a.appointmentDateTime)) === dateKey
      );

      days.push({
        date: d,
        dateKey,
        isToday: this.isToday(year, month, d),
        isSelected: dateKey === this.selectedDateKey(),
        isEmpty: false,
        appointments: dayAppointments
      });
    }

    return days;
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
   * Check if date is today
   */
  private isToday(year: number, month: number, day: number): boolean {
    return day === this.today.getDate() && 
           month === this.today.getMonth() && 
           year === this.today.getFullYear();
  }

  /**
   * Navigate to previous month
   */
  previousMonth(): void {
    let month = this.viewMonth() - 1;
    let year = this.viewYear();
    
    if (month < 0) {
      month = 11;
      year--;
    }
    
    this.viewMonth.set(month);
    this.viewYear.set(year);
  }

  /**
   * Navigate to next month
   */
  nextMonth(): void {
    let month = this.viewMonth() + 1;
    let year = this.viewYear();
    
    if (month > 11) {
      month = 0;
      year++;
    }
    
    this.viewMonth.set(month);
    this.viewYear.set(year);
  }

  /**
   * Go to today
   */
  goToToday(): void {
    this.viewYear.set(this.today.getFullYear());
    this.viewMonth.set(this.today.getMonth());
    
    const todayKey = this.getDateKey(this.today);
    this.selectedDateKey.set(todayKey);
  }

  /**
   * Select a day
   */
  selectDay(day: CalendarDay): void {
    if (day.isEmpty) return;
    this.selectedDateKey.set(day.dateKey);
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
    return map[status] || 'badge-scheduled';
  }

  /**
   * Get appointment card class
   */
  getCardClass(status: string): string {
    return status.toLowerCase();
  }

  /**
   * Format time from datetime
   */
  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format appointment type
   */
  formatType(type: string): string {
    return type.replace(/_/g, ' ');
  }

  /**
   * Get current date formatted
   */
  getCurrentDate(): string {
    return this.today.toLocaleDateString('en-GB', {
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
